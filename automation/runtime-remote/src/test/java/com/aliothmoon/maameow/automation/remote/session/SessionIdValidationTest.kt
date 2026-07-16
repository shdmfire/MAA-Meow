package com.aliothmoon.maameow.automation.remote.session

import com.aliothmoon.maameow.automation.ipc.RemoteSessionRequest
import com.aliothmoon.maameow.automation.remote.device.RemoteDisplaySession
import com.aliothmoon.maameow.automation.remote.device.RemoteDisplaySessionFactory
import com.aliothmoon.maameow.automation.remote.engine.RemoteControllerDescriptor
import com.aliothmoon.maameow.automation.remote.engine.RemoteControllerEngine
import com.aliothmoon.maameow.automation.remote.engine.RemoteControllerEngineFactory
import com.aliothmoon.maameow.automation.remote.engine.RemoteControllerEngineRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionIdValidationTest {

    private class FakeEngine : RemoteControllerEngine
    private class FakeEngineFactory : RemoteControllerEngineFactory {
        override val descriptor = RemoteControllerDescriptor(controllerId = "test-controller")
        override fun create(request: RemoteSessionRequest): RemoteControllerEngine = FakeEngine()
    }

    private class FakeDisplaySession : RemoteDisplaySession {
        override val displayId: Int = 1
        override val width: Int = 1920
        override val height: Int = 1080
        override fun stop() {}
    }

    private class FakeDisplaySessionFactory : RemoteDisplaySessionFactory {
        override fun start(mode: Int, width: Int, height: Int, dpi: Int): RemoteDisplaySession = FakeDisplaySession()
    }

    @Test
    fun `invalid sessionId is rejected for operations`() {
        val registry = RemoteControllerEngineRegistry(listOf(FakeEngineFactory()))
        val coordinator = RemoteSessionCoordinator(
            registry = registry,
            displaySessionFactory = FakeDisplaySessionFactory()
        )

        val request = RemoteSessionRequest(controllerId = "test-controller", requestType = "START")
        val info = coordinator.startSession(request)
        val realSessionId = info.sessionId
        val fakeSessionId = "wrong-session-id"

        // Test validate
        assertNull(coordinator.validate(fakeSessionId))

        // Test setMonitorSurface
        assertFalse(coordinator.setMonitorSurface(fakeSessionId, null))

        // Test clearMonitorSurface
        assertFalse(coordinator.clearMonitorSurface(fakeSessionId))

        // Test touch
        // Wait, touch checks if mode is DISPLAY_MODE_BACKGROUND. Let's set it.
        coordinator.setDisplayMode(RemoteSessionCoordinator.DISPLAY_MODE_BACKGROUND)
        assertFalse(coordinator.touchDown(fakeSessionId, 100, 100))
        assertFalse(coordinator.touchMove(fakeSessionId, 100, 100))
        assertFalse(coordinator.touchUp(fakeSessionId, 100, 100))
    }
}
