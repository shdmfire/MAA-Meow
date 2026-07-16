package com.aliothmoon.maameow.automation.app

import android.os.IBinder
import android.view.Surface
import com.aliothmoon.maameow.automation.ipc.IRemoteAutomationCallback
import com.aliothmoon.maameow.automation.ipc.IRemoteAutomationService
import com.aliothmoon.maameow.automation.ipc.ITouchEventCallback
import com.aliothmoon.maameow.automation.ipc.PermissionGrantRequest
import com.aliothmoon.maameow.automation.ipc.PermissionStateInfo
import com.aliothmoon.maameow.automation.ipc.RemoteSessionInfo
import com.aliothmoon.maameow.automation.ipc.RemoteSessionRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class AutomationSessionCoordinatorTest {

    private open class FakeRemoteAutomationService : IRemoteAutomationService {
        override fun asBinder(): IBinder? = null
        override fun destroy() {}
        override fun pid(): Int = 1234
        override fun runtimeVersion(): String = "1.0.0"
        override fun grantPermissions(request: PermissionGrantRequest?): PermissionStateInfo = PermissionStateInfo()
        override fun setForcedDisplaySize(width: Int, height: Int): Boolean = true
        override fun clearForcedDisplaySize(): Boolean = true
        override fun setMonitorSurface(surface: Surface?) {}
        override fun setVirtualDisplayMode(mode: Int): Boolean = true
        override fun setVirtualDisplayResolution(width: Int, height: Int, dpi: Int) {}
        override fun startVirtualDisplay(): Int = 1
        override fun stopVirtualDisplay() {}
        override fun touchDown(x: Int, y: Int) {}
        override fun touchMove(x: Int, y: Int) {}
        override fun touchUp(x: Int, y: Int) {}
        override fun setDisplayPower(on: Boolean) {}
        override fun setPlayAudioOpAllowed(packageName: String?, isAllowed: Boolean): Boolean = true
        override fun isAppAlive(packageName: String?): Int = 0
        override fun heartbeat(appPid: Int) {}
        override fun setTouchCallback(callback: ITouchEventCallback?) {}
        override fun startActivity(intent: android.content.Intent?): Boolean = true
        override fun isPackageInstalled(packageName: String?): Boolean = true
        override fun isAppOnVirtualDisplay(packageName: String?): Boolean = false
        override fun setForceFullscreenOnVirtualDisplay(enabled: Boolean) {}
        override fun captureFramePng(dirPath: String?): String? = null
        override fun startSession(request: RemoteSessionRequest): RemoteSessionInfo =
            RemoteSessionInfo("session-123", "RUNNING")
        override fun stopSession(sessionId: String): RemoteSessionInfo =
            RemoteSessionInfo(sessionId, "IDLE")

        override fun installedControllerIds(): Array<String> = emptyArray()
        override fun startSessionWithCallback(
            request: RemoteSessionRequest?,
            callback: IRemoteAutomationCallback?
        ): RemoteSessionInfo = RemoteSessionInfo("session-123", "RUNNING")
        override fun getActiveSession(): RemoteSessionInfo = RemoteSessionInfo("", "IDLE")
        override fun setMonitorSurfaceForSession(sessionId: String?, surface: Surface?): Boolean = true
        override fun clearMonitorSurface(sessionId: String?): Boolean = true
        override fun touchDownForSession(sessionId: String?, x: Int, y: Int): Boolean = true
        override fun touchMoveForSession(sessionId: String?, x: Int, y: Int): Boolean = true
        override fun touchUpForSession(sessionId: String?, x: Int, y: Int): Boolean = true
        override fun captureFramePngForSession(sessionId: String?, dirPath: String?): String? = null
    }

    @Test
    fun `start session successfully stores active session`() {
        val service = FakeRemoteAutomationService()
        val client = RemoteAutomationClient { service }
        val store = ActiveSessionStore()
        val coordinator = AutomationSessionCoordinator(client, store)

        assertNull(store.get())
        val request = RemoteSessionRequest(controllerId = "maa-legacy", requestType = "START")
        val session = coordinator.start(request)

        assertNotNull(session)
        assertEquals("session-123", session?.sessionId)
        assertEquals("maa-legacy", session?.controllerId)
        assertEquals(session, store.get())
    }

    @Test
    fun `cannot start session if one is already active`() {
        val service = FakeRemoteAutomationService()
        val client = RemoteAutomationClient { service }
        val store = ActiveSessionStore()
        val coordinator = AutomationSessionCoordinator(client, store)

        val request = RemoteSessionRequest(controllerId = "maa-legacy", requestType = "START")
        val session1 = coordinator.start(request)
        assertNotNull(session1)

        val session2 = coordinator.start(request)
        assertNull(session2)
    }

    @Test
    fun `stop session cleans up store`() {
        val stopCalled = AtomicBoolean(false)
        val service = object : FakeRemoteAutomationService() {
            override fun stopSession(sessionId: String): RemoteSessionInfo {
                stopCalled.set(true)
                return super.stopSession(sessionId)
            }
        }
        val client = RemoteAutomationClient { service }
        val store = ActiveSessionStore()
        val coordinator = AutomationSessionCoordinator(client, store)

        val request = RemoteSessionRequest(controllerId = "maa-legacy", requestType = "START")
        coordinator.start(request)
        assertNotNull(store.get())

        val stopped = coordinator.stop()
        assertTrue(stopped)
        assertTrue(stopCalled.get())
        assertNull(store.get())
    }
}
