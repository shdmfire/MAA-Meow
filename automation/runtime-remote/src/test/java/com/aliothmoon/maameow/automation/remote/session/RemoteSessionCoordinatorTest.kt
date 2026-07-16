package com.aliothmoon.maameow.automation.remote.session

import com.aliothmoon.maameow.automation.ipc.RemoteSessionInfo
import com.aliothmoon.maameow.automation.ipc.RemoteSessionRequest
import com.aliothmoon.maameow.automation.remote.device.RemoteDisplaySession
import com.aliothmoon.maameow.automation.remote.device.RemoteDisplaySessionFactory
import com.aliothmoon.maameow.automation.remote.engine.RemoteControllerDescriptor
import com.aliothmoon.maameow.automation.remote.engine.RemoteControllerEngine
import com.aliothmoon.maameow.automation.remote.engine.RemoteControllerEngineFactory
import com.aliothmoon.maameow.automation.remote.engine.RemoteControllerEngineRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class RemoteSessionCoordinatorTest {

    private class FakeEngine(
        val onStop: () -> Unit = {},
        val onDestroy: () -> Unit = {}
    ) : RemoteControllerEngine {
        override suspend fun stop() {
            onStop()
        }
        override fun destroy() {
            onDestroy()
        }
    }

    private class FakeEngineFactory(val engine: RemoteControllerEngine) : RemoteControllerEngineFactory {
        override val descriptor = RemoteControllerDescriptor(controllerId = "test-controller")
        override fun create(request: RemoteSessionRequest): RemoteControllerEngine = engine
    }

    private class FakeDisplaySession : RemoteDisplaySession {
        override val displayId: Int = 1
        override val width: Int = 1920
        override val height: Int = 1080
        var stopped = false
        override fun stop() {
            stopped = true
        }
    }

    private class FakeDisplaySessionFactory(val session: FakeDisplaySession) : RemoteDisplaySessionFactory {
        override fun start(mode: Int, width: Int, height: Int, dpi: Int): RemoteDisplaySession = session
    }

    @Test
    fun `empty registry returns CONTROLLER_NOT_FOUND`() {
        val registry = RemoteControllerEngineRegistry(emptyList())
        val coordinator = RemoteSessionCoordinator(registry = registry)
        val request = RemoteSessionRequest(controllerId = "non-existent", requestType = "START")
        val info = coordinator.startSession(request)
        assertEquals(RemoteSessionState.ERROR.name, info.state)
        assertEquals(RemoteSessionErrorCode.CONTROLLER_NOT_FOUND, info.errorCode)
    }

    @Test
    fun `double start returns SESSION_BUSY`() {
        val engine = FakeEngine()
        val registry = RemoteControllerEngineRegistry(listOf(FakeEngineFactory(engine)))
        val displaySession = FakeDisplaySession()
        val displayFactory = FakeDisplaySessionFactory(displaySession)
        val coordinator = RemoteSessionCoordinator(
            registry = registry,
            displaySessionFactory = displayFactory
        )

        val request = RemoteSessionRequest(controllerId = "test-controller", requestType = "START")
        val firstInfo = coordinator.startSession(request)
        assertEquals(RemoteSessionState.RUNNING.name, firstInfo.state)

        val secondInfo = coordinator.startSession(request)
        assertEquals(RemoteSessionState.ERROR.name, secondInfo.state)
        assertEquals(RemoteSessionErrorCode.SESSION_BUSY, secondInfo.errorCode)
    }

    @Test
    fun `stopSession cleans up active session`() {
        val engineStopped = AtomicBoolean(false)
        val engineDestroyed = AtomicBoolean(false)
        val engine = FakeEngine(
            onStop = { engineStopped.set(true) },
            onDestroy = { engineDestroyed.set(true) }
        )
        val registry = RemoteControllerEngineRegistry(listOf(FakeEngineFactory(engine)))
        val displaySession = FakeDisplaySession()
        val displayFactory = FakeDisplaySessionFactory(displaySession)
        val coordinator = RemoteSessionCoordinator(
            registry = registry,
            displaySessionFactory = displayFactory
        )

        val request = RemoteSessionRequest(controllerId = "test-controller", requestType = "START")
        val startInfo = coordinator.startSession(request)
        assertNotNull(startInfo.sessionId)

        val stopInfo = coordinator.stopSession(startInfo.sessionId)
        assertEquals(RemoteSessionState.IDLE.name, stopInfo.state)
        assertTrue(engineStopped.get())
        assertTrue(engineDestroyed.get())
        assertTrue(displaySession.stopped)
        assertEquals(RemoteSessionState.IDLE.name, coordinator.getActiveSession().state)
    }
}
