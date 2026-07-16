package com.aliothmoon.maameow.automation.remote

import android.graphics.Bitmap
import android.os.Process
import android.view.Surface
import com.aliothmoon.maameow.automation.ipc.IRemoteAutomationService
import com.aliothmoon.maameow.automation.ipc.IRemoteAutomationCallback
import com.aliothmoon.maameow.automation.ipc.ITouchEventCallback
import com.aliothmoon.maameow.automation.ipc.PermissionGrantRequest
import com.aliothmoon.maameow.automation.ipc.PermissionStateInfo
import com.aliothmoon.maameow.automation.ipc.RemoteSessionInfo
import com.aliothmoon.maameow.automation.ipc.RemoteSessionRequest
import com.aliothmoon.maameow.automation.remote.bridge.NativeBridgeLib
import com.aliothmoon.maameow.automation.remote.RemoteDisplayDefaults

import com.aliothmoon.maameow.automation.remote.input.InputControlUtils
import android.content.Intent
import com.aliothmoon.maameow.automation.remote.internal.ActivityUtils
import com.aliothmoon.maameow.automation.remote.internal.GameAudioMuteController
import com.aliothmoon.maameow.automation.remote.internal.PermissionGrantHelper
import com.aliothmoon.maameow.automation.remote.internal.PowerController
import com.aliothmoon.maameow.automation.remote.internal.PrimaryDisplayManager
import com.aliothmoon.maameow.automation.remote.internal.RemoteUtils
import com.aliothmoon.maameow.automation.remote.internal.ScreenManager
import com.aliothmoon.maameow.automation.remote.internal.VirtualDisplayManager
import com.aliothmoon.maameow.automation.remote.third.FakeContext
import com.aliothmoon.maameow.automation.remote.third.Ln
import com.aliothmoon.maameow.automation.remote.third.Workarounds
import com.aliothmoon.maameow.automation.remote.session.RemoteSessionCoordinator
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

class RemoteAutomationServiceImpl : IRemoteAutomationService.Stub() {

    companion object {
        private const val TAG = "RemoteAutomationService"
        private const val HEARTBEAT_INTERVAL_MS = 5_000L
        private const val DISPLAY_MODE_PRIMARY = 0
        private const val DISPLAY_MODE_BACKGROUND = 1

        @JvmStatic
        fun performEmergencyCleanup() {
            Ln.i("$TAG: performEmergencyCleanup triggered")
            runCatching {
                GameAudioMuteController.restoreAll()
                PowerController.destroy()
                ScreenManager.destroy()
                Unit
            }.onFailure {
                Ln.e("$TAG: Emergency cleanup failed: ${it.message}")
            }
        }
    }

    init {
        RemoteBootTrace.mark("CTOR_START")
        Runtime.getRuntime().addShutdownHook(Thread {
            runCatching { performEmergencyCleanup() }
        }.apply { name = "remote-shutdown-hook" })
    }

    private val virtualDisplayMode = AtomicInteger(DISPLAY_MODE_PRIMARY)
    private val appPid = AtomicInteger(0)
    private val destroyed = AtomicBoolean(false)
    private val sessionCoordinator = RemoteSessionCoordinator()

    init {
        Workarounds.apply()
        PermissionGrantHelper.disablePhantomProcessKiller()
        startHeartbeatWatchdog()
    }

    override fun destroy() {
        if (!destroyed.compareAndSet(false, true)) {
            return
        }
        Ln.i("$TAG: destroy()")
        runCatching { sessionCoordinator.cleanupAll() }
        InputControlUtils.setTouchCallback(null)
        performEmergencyCleanup()
        exitProcess(0)
    }

    override fun runtimeVersion(): String = NativeBridgeLib.ping()

    override fun pid(): Int = Process.myPid()



    override fun captureFramePng(dirPath: String?): String? {
        if (rejectLegacyDisplayOperation("captureFramePng")) return null
        return doCaptureFramePng(dirPath)
    }

    private fun doCaptureFramePng(dirPath: String?): String? {
        if (dirPath.isNullOrBlank()) {
            Ln.w("$TAG: captureFramePng - blank dirPath")
            return null
        }
        val bitmap = NativeBridgeLib.getFrameBufferBitmap() ?: run {
            Ln.w("$TAG: captureFramePng - no frame available")
            return null
        }
        return try {
            val dir = File(dirPath).apply { mkdirs() }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
            val file = File(dir, "screenshot_$timestamp.png")
            val ok = FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            if (!ok) {
                Ln.e("$TAG: captureFramePng - PNG compress failed")
                file.delete()
                return null
            }
            Ln.i("$TAG: captureFramePng saved ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Ln.e("$TAG: captureFramePng error: ${e.message}")
            null
        } finally {
            bitmap.recycle()
        }
    }

    override fun setForcedDisplaySize(width: Int, height: Int): Boolean {
        return ScreenManager.setForcedDisplaySize(width, height)
    }

    override fun clearForcedDisplaySize(): Boolean {
        return ScreenManager.clearForcedDisplaySize()
    }

    override fun grantPermissions(request: PermissionGrantRequest): PermissionStateInfo {
        val packageName = request.packageName
        val uid = if (request.uid > 0) request.uid
        else RemoteUtils.getAppUid(packageName).takeIf { it > 0 } ?: request.uid
        val p = request.permissions

        with(PermissionGrantHelper) {
            return PermissionStateInfo(
                accessibilityPermission = if (p and PermissionGrantRequest.PERM_ACCESSIBILITY != 0) grantAccessibilityService(
                    request.accessibilityServiceId
                ) else false,
                floatingWindowPermission = if (p and PermissionGrantRequest.PERM_FLOATING_WINDOW != 0) grantFloatingWindowPermission(
                    packageName,
                    uid
                ) else false,
                notificationPermission = if (p and PermissionGrantRequest.PERM_NOTIFICATION != 0) grantNotificationPermission(
                    packageName,
                    uid
                ) else false,
                batteryOptimizationExempt = if (p and PermissionGrantRequest.PERM_BATTERY != 0) grantBatteryOptimizationExemption(
                    packageName
                ) else false,
                storagePermission = if (p and PermissionGrantRequest.PERM_STORAGE != 0) grantStoragePermission(
                    packageName,
                    uid
                ) else false,
                backgroundUnrestricted = if (p and PermissionGrantRequest.PERM_BACKGROUND != 0) grantBackgroundUnrestricted(
                    packageName,
                    uid
                ) else false,
            )
        }
    }

    override fun setMonitorSurface(surface: Surface?) {
        if (rejectLegacyDisplayOperation("setMonitorSurface")) return
        Ln.i("$TAG: setMonitorSurface(${surface != null})")
        VirtualDisplayManager.setMonitorSurface(surface)
        NativeBridgeLib.setPreviewSurface(surface)
    }

    override fun setTouchCallback(callback: ITouchEventCallback?) {
        Ln.i("$TAG: setTouchCallback(${callback != null})")
        InputControlUtils.setTouchCallback(callback)
    }

    override fun touchDown(x: Int, y: Int) {
        if (rejectLegacyDisplayOperation("touchDown")) return
        if (virtualDisplayMode.get() == DISPLAY_MODE_PRIMARY) return
        val displayId = VirtualDisplayManager.getDisplayId()
        if (displayId != RemoteDisplayDefaults.DISPLAY_NONE) {
            InputControlUtils.down(x, y, displayId)
        }
    }

    override fun touchMove(x: Int, y: Int) {
        if (rejectLegacyDisplayOperation("touchMove")) return
        if (virtualDisplayMode.get() == DISPLAY_MODE_PRIMARY) return
        val displayId = VirtualDisplayManager.getDisplayId()
        if (displayId != RemoteDisplayDefaults.DISPLAY_NONE) {
            InputControlUtils.move(x, y, displayId)
        }
    }

    override fun touchUp(x: Int, y: Int) {
        if (rejectLegacyDisplayOperation("touchUp")) return
        if (virtualDisplayMode.get() == DISPLAY_MODE_PRIMARY) return
        val displayId = VirtualDisplayManager.getDisplayId()
        if (displayId != RemoteDisplayDefaults.DISPLAY_NONE) {
            InputControlUtils.up(x, y, displayId)
        }
    }

    private fun rejectLegacyDisplayOperation(operation: String): Boolean {
        if (!sessionCoordinator.hasActiveSession()) return false
        Ln.w("$TAG: reject legacy $operation while session is active")
        return true
    }

    override fun setDisplayPower(on: Boolean) {
        PowerController.setDisplayPower(on)
    }

    override fun startVirtualDisplay(): Int {
        Ln.i("$TAG: startVirtualDisplay() ${virtualDisplayMode.get()}")
        return when (virtualDisplayMode.get()) {
            DISPLAY_MODE_PRIMARY -> PrimaryDisplayManager.start()
            DISPLAY_MODE_BACKGROUND -> VirtualDisplayManager.start().also { displayId ->
                if (displayId != RemoteDisplayDefaults.DISPLAY_NONE) {
                    PowerController.startUserActivityKeepAlive(displayId)
                }
            }

            else -> RemoteDisplayDefaults.DISPLAY_NONE
        }
    }

    override fun stopVirtualDisplay() {
        Ln.i("$TAG: stopVirtualDisplay() ${virtualDisplayMode.get()}")
        when (virtualDisplayMode.get()) {
            DISPLAY_MODE_PRIMARY -> PrimaryDisplayManager.stop()
            DISPLAY_MODE_BACKGROUND -> {
                PowerController.stopUserActivityKeepAlive()
                VirtualDisplayManager.stop()
            }
        }
        GameAudioMuteController.restoreAll()
    }

    override fun setPlayAudioOpAllowed(packageName: String?, isAllowed: Boolean): Boolean {
        if (packageName.isNullOrBlank()) return false
        val ok = GameAudioMuteController.setMuted(packageName, muted = !isAllowed)
        if (!ok) {
            Ln.w("$TAG: setPlayAudioOpAllowed($packageName, allowed=$isAllowed) failed")
        }
        return ok
    }

    override fun isAppAlive(packageName: String): Int {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("pidof", packageName))
            val exitCode = process.waitFor()
            val output = process.inputStream.bufferedReader().readText().trim()
            val errorOutput = process.errorStream.bufferedReader().readText().trim()
            when (exitCode) {
                0 if output.isNotEmpty() -> AppAliveStatus.ALIVE
                1 if output.isEmpty() && errorOutput.isEmpty() -> AppAliveStatus.DEAD
                else -> {
                    Ln.w(
                        "$TAG: isAppAlive unexpected result for $packageName: exitCode=$exitCode, stdout=$output, stderr=$errorOutput"
                    )
                    AppAliveStatus.UNKNOWN
                }
            }
        } catch (e: Exception) {
            Ln.w("isAppAlive check failed for $packageName", e)
            AppAliveStatus.UNKNOWN
        }
    }

    override fun heartbeat(pid: Int) {
        appPid.set(pid)
        Ln.i("$TAG: heartbeat received, app pid=$pid")
    }

    override fun isAppOnVirtualDisplay(packageName: String): Boolean {
        val targetDisplayId = VirtualDisplayManager.getDisplayId()
        if (targetDisplayId == RemoteDisplayDefaults.DISPLAY_NONE) return true
        return ActivityUtils.isAppOnDisplay(packageName, targetDisplayId)
    }

    override fun isPackageInstalled(packageName: String): Boolean {
        return try {
            FakeContext.get().packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            Ln.w("$TAG: isPackageInstalled: $packageName not found", e)
            false
        }
    }

    override fun startActivity(intent: Intent): Boolean {
        return ActivityUtils.startActivity(intent)
    }

    override fun setForceFullscreenOnVirtualDisplay(enabled: Boolean) {
        Ln.i("$TAG: setForceFullscreenOnVirtualDisplay($enabled)")
        ActivityUtils.forceFullscreenOnVirtualDisplay = enabled
    }

    override fun setVirtualDisplayResolution(width: Int, height: Int, dpi: Int) {
        Ln.i("$TAG: setVirtualDisplayResolution(${width}x${height}, dpi=$dpi)")
        if (!sessionCoordinator.setResolution(width, height, dpi)) {
            Ln.w("$TAG: reject resolution change while session is running")
            return
        }
        VirtualDisplayManager.setResolution(width, height, dpi)
    }

    override fun setVirtualDisplayMode(mode: Int): Boolean {
        when (mode) {
            DISPLAY_MODE_PRIMARY -> {
                if (!sessionCoordinator.setDisplayMode(mode)) return false
                VirtualDisplayManager.stop()
                virtualDisplayMode.set(mode)
                return true
            }

            DISPLAY_MODE_BACKGROUND -> {
                if (!sessionCoordinator.setDisplayMode(mode)) return false
                PrimaryDisplayManager.stop()
                virtualDisplayMode.set(mode)
                return true
            }
        }
        return false
    }

    private fun startHeartbeatWatchdog() {
        Thread {
            while (!destroyed.get()) {
                try {
                    Thread.sleep(HEARTBEAT_INTERVAL_MS)
                } catch (_: InterruptedException) {
                    return@Thread
                }
                val pid = appPid.get()
                if (pid <= 0) {
                    continue
                }
                if (!File("/proc/$pid").exists()) {
                    Ln.w("$TAG: app process (pid=$pid) no longer exists, destroying remote service")
                    destroy()
                    return@Thread
                }
            }
        }.apply {
            name = "remote-heartbeat-watchdog"
            isDaemon = true
        }.start()
    }

    override fun installedControllerIds(): Array<String> = sessionCoordinator.installedControllerIds().toTypedArray()

    override fun getActiveSession(): RemoteSessionInfo = sessionCoordinator.getActiveSession()

    override fun startSession(request: RemoteSessionRequest): RemoteSessionInfo =
        sessionCoordinator.startSession(request, null)

    override fun startSessionWithCallback(
        request: RemoteSessionRequest,
        callback: IRemoteAutomationCallback?
    ): RemoteSessionInfo = sessionCoordinator.startSession(request, callback)

    override fun stopSession(sessionId: String): RemoteSessionInfo = sessionCoordinator.stopSession(sessionId)

    override fun setMonitorSurfaceForSession(sessionId: String, surface: Surface?): Boolean =
        sessionCoordinator.setMonitorSurface(sessionId, surface)

    override fun clearMonitorSurface(sessionId: String): Boolean =
        sessionCoordinator.clearMonitorSurface(sessionId)

    override fun touchDownForSession(sessionId: String, x: Int, y: Int): Boolean =
        sessionCoordinator.touchDown(sessionId, x, y)

    override fun touchMoveForSession(sessionId: String, x: Int, y: Int): Boolean =
        sessionCoordinator.touchMove(sessionId, x, y)

    override fun touchUpForSession(sessionId: String, x: Int, y: Int): Boolean =
        sessionCoordinator.touchUp(sessionId, x, y)

    override fun captureFramePngForSession(sessionId: String, dirPath: String?): String? =
        if (sessionCoordinator.validate(sessionId) != null) doCaptureFramePng(dirPath) else null

}
