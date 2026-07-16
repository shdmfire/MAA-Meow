package com.aliothmoon.maameow.automation.remote.session

import android.content.Intent
import android.view.Surface
import com.aliothmoon.maameow.automation.ipc.IRemoteAutomationCallback
import com.aliothmoon.maameow.automation.ipc.RemoteSessionInfo
import com.aliothmoon.maameow.automation.ipc.RemoteSessionRequest
import com.aliothmoon.maameow.automation.remote.RemoteDisplayDefaults
import com.aliothmoon.maameow.automation.remote.device.RemoteDeviceEnvironment
import com.aliothmoon.maameow.automation.remote.device.RemoteDisplaySession
import com.aliothmoon.maameow.automation.remote.engine.EnginePrepareResult
import com.aliothmoon.maameow.automation.remote.engine.EngineStartResult
import com.aliothmoon.maameow.automation.remote.engine.RemoteControllerEngineRegistry
import com.aliothmoon.maameow.automation.remote.frame.RemoteFrameSource
import com.aliothmoon.maameow.automation.remote.input.InputControlUtils
import com.aliothmoon.maameow.automation.remote.bridge.NativeBridgeLib
import com.aliothmoon.maameow.automation.remote.internal.ActivityUtils
import com.aliothmoon.maameow.automation.remote.internal.PrimaryDisplayManager
import com.aliothmoon.maameow.automation.remote.internal.VirtualDisplayManager
import com.aliothmoon.maameow.automation.remote.internal.PowerController
import kotlinx.coroutines.runBlocking
import java.util.UUID

import com.aliothmoon.maameow.automation.remote.device.RemoteDisplaySessionFactory

class RemoteSessionCoordinator(
    private val registry: RemoteControllerEngineRegistry = RemoteControllerEngineRegistry(),
    private val cleanup: RemoteSessionCleanup = RemoteSessionCleanup(),
    private val displaySessionFactory: RemoteDisplaySessionFactory = object : RemoteDisplaySessionFactory {
        override fun start(mode: Int, width: Int, height: Int, dpi: Int): RemoteDisplaySession {
            if (mode == DISPLAY_MODE_BACKGROUND) VirtualDisplayManager.setResolution(width, height, dpi)
            val displayId = if (mode == DISPLAY_MODE_BACKGROUND) {
                VirtualDisplayManager.start().also {
                    if (it != RemoteDisplayDefaults.DISPLAY_NONE) {
                        PowerController.startUserActivityKeepAlive(it)
                    }
                }
            } else {
                PrimaryDisplayManager.start()
            }
            return object : RemoteDisplaySession {
                override val displayId = displayId
                override val width = width
                override val height = height
                override fun stop() {
                    if (mode == DISPLAY_MODE_BACKGROUND) VirtualDisplayManager.stop() else PrimaryDisplayManager.stop()
                }
            }
        }
    }
) {
    private var active: RemoteSession? = null
    private var mode: Int = DISPLAY_MODE_PRIMARY
    private var width: Int = RemoteDisplayDefaults.WIDTH
    private var height: Int = RemoteDisplayDefaults.HEIGHT
    private var dpi: Int = RemoteDisplayDefaults.DPI
    private var monitorSurfaceSessionId: String? = null

    @Synchronized fun installedControllerIds(): List<String> = registry.controllerIds()
    @Synchronized fun getActiveSession(): RemoteSessionInfo = active?.toInfo() ?: RemoteSessionInfo(state = RemoteSessionState.IDLE.name)

    @Synchronized
    fun hasActiveSession(): Boolean = active != null

    @Synchronized
    fun setDisplayMode(mode: Int): Boolean {
        if (active != null) return false
        if (mode != DISPLAY_MODE_PRIMARY && mode != DISPLAY_MODE_BACKGROUND) return false
        this.mode = mode
        return true
    }

    @Synchronized fun setResolution(width: Int, height: Int, dpi: Int): Boolean { if (active != null) return false; this.width = width; this.height = height; this.dpi = dpi; return true }

    @Synchronized fun startSession(request: RemoteSessionRequest, callback: IRemoteAutomationCallback? = null): RemoteSessionInfo {
        if (active != null) return RemoteSessionInfo(state = RemoteSessionState.ERROR.name, errorCode = RemoteSessionErrorCode.SESSION_BUSY, message = "remote session is already running")
        val factory = registry.factory(request.controllerId) ?: return RemoteSessionInfo(state = RemoteSessionState.ERROR.name, errorCode = RemoteSessionErrorCode.CONTROLLER_NOT_FOUND, message = "controller '${request.controllerId}' is not installed")
        val display = displaySessionFactory.start(mode, width, height, dpi)
        if (display.displayId == RemoteDisplayDefaults.DISPLAY_NONE) {
            runCatching { display.stop() }
            return RemoteSessionInfo(state = RemoteSessionState.ERROR.name, errorCode = RemoteSessionErrorCode.DISPLAY_START_FAILED, message = "display session failed to start")
        }
        val engine = factory.create(request)
        val session = RemoteSession(UUID.randomUUID().toString(), request.controllerId, engine, display)
        active = session
        val env = Environment(session)
        return try {
            when (val r = runBlocking { engine.prepare(request, env) }) {
                EnginePrepareResult.Ready -> Unit
                is EnginePrepareResult.Failed -> return failStart(session, r.errorCode, r.message)
            }
            when (val r = runBlocking { engine.start { callback?.onEvent(it) } }) {
                EngineStartResult.Started -> { session.state = RemoteSessionState.RUNNING; session.toInfo() }
                is EngineStartResult.Failed -> failStart(session, r.errorCode, r.message)
            }
        } catch (t: Throwable) {
            failStart(session, RemoteSessionErrorCode.ENGINE_START_FAILED, t.message)
        }
    }

    @Synchronized fun stopSession(sessionId: String): RemoteSessionInfo {
        val session = active ?: return RemoteSessionInfo(sessionId = sessionId, state = RemoteSessionState.ERROR.name, errorCode = RemoteSessionErrorCode.SESSION_NOT_FOUND)
        if (session.sessionId != sessionId) return RemoteSessionInfo(sessionId = sessionId, state = RemoteSessionState.ERROR.name, errorCode = RemoteSessionErrorCode.INVALID_SESSION)
        session.state = RemoteSessionState.STOPPING
        cleanup.cleanup(session)
        active = null
        monitorSurfaceSessionId = null
        setPreviewSurface(null)
        return RemoteSessionInfo(sessionId = sessionId, state = RemoteSessionState.IDLE.name)
    }

    @Synchronized fun cleanupAll() { cleanup.cleanup(active); active = null; monitorSurfaceSessionId = null; setPreviewSurface(null) }
    @Synchronized fun validate(sessionId: String): RemoteSession? = active?.takeIf { it.sessionId == sessionId && it.state == RemoteSessionState.RUNNING }
    @Synchronized fun setMonitorSurface(sessionId: String, surface: Surface?): Boolean { validate(sessionId) ?: return false; monitorSurfaceSessionId = sessionId; VirtualDisplayManager.setMonitorSurface(surface); setPreviewSurface(surface); return true }
    @Synchronized fun clearMonitorSurface(sessionId: String): Boolean { if (monitorSurfaceSessionId != sessionId) return false; VirtualDisplayManager.setMonitorSurface(null); setPreviewSurface(null); monitorSurfaceSessionId = null; return true }
    fun touchDown(sessionId: String, x: Int, y: Int) = touch(sessionId, x, y) { a,b,c -> InputControlUtils.down(a,b,c) }
    fun touchMove(sessionId: String, x: Int, y: Int) = touch(sessionId, x, y) { a,b,c -> InputControlUtils.move(a,b,c) }
    fun touchUp(sessionId: String, x: Int, y: Int) = touch(sessionId, x, y) { a,b,c -> InputControlUtils.up(a,b,c) }

    private fun touch(sessionId: String, x: Int, y: Int, op: (Int, Int, Int) -> Unit): Boolean { val s = validate(sessionId) ?: return false; if (mode != DISPLAY_MODE_BACKGROUND) return false; if (x !in 0 until s.display.width || y !in 0 until s.display.height) return false; op(x, y, s.display.displayId); return true }
    private fun failStart(session: RemoteSession, code: String, message: String?): RemoteSessionInfo { cleanup.cleanup(session); active = null; setPreviewSurface(null); return RemoteSessionInfo(session.sessionId, RemoteSessionState.ERROR.name, code, message) }
    private fun setPreviewSurface(surface: Surface?) { runCatching { NativeBridgeLib.setPreviewSurface(surface) } }
    private fun RemoteSession.toInfo() = RemoteSessionInfo(sessionId, state.name)

    private inner class Environment(private val session: RemoteSession) : RemoteDeviceEnvironment { override val display = session.display; override val frameSource = RemoteFrameSource { null }; override fun startApp(intent: Intent) = ActivityUtils.startActivity(intent); override fun touchDown(x: Int, y: Int) = this@RemoteSessionCoordinator.touchDown(session.sessionId, x, y); override fun touchMove(x: Int, y: Int) = this@RemoteSessionCoordinator.touchMove(session.sessionId, x, y); override fun touchUp(x: Int, y: Int) = this@RemoteSessionCoordinator.touchUp(session.sessionId, x, y) }
    companion object { const val DISPLAY_MODE_PRIMARY = 0; const val DISPLAY_MODE_BACKGROUND = 1 }
}
