package com.aliothmoon.maameow.automation.remote.engine

import com.aliothmoon.maameow.automation.ipc.RemoteSessionRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemoteControllerEngineRegistryTest {

    private class FakeEngineFactory(private val id: String) : RemoteControllerEngineFactory {
        override val descriptor = RemoteControllerDescriptor(controllerId = id)
        override fun create(request: RemoteSessionRequest): RemoteControllerEngine {
            return object : RemoteControllerEngine {}
        }
    }

    @Test
    fun `empty registry starts normally`() {
        val registry = RemoteControllerEngineRegistry(emptyList())
        assertEquals(0, registry.controllerIds().size)
        assertNull(registry.factory("any"))
    }

    @Test
    fun `duplicate controller id logs error and keeps first`() {
        val factories = listOf(
            FakeEngineFactory("id-1"),
            FakeEngineFactory("id-1")
        )
        val registry = RemoteControllerEngineRegistry(factories)
        assertEquals(1, registry.controllerIds().size)
        assertEquals("id-1", registry.controllerIds().first())
    }
}
