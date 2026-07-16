package com.aliothmoon.maameow.automation.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ControllerDescriptorTest {
    @Test
    fun `descriptor exposes stable identity and capabilities`() {
        val descriptor = ControllerDescriptor(
            controllerId = ControllerId("sample"),
            displayName = "Sample controller",
            capabilities = setOf(ControllerCapability.START, ControllerCapability.STOP),
        )

        assertEquals("sample", descriptor.controllerId.value)
        assertEquals(1, descriptor.schemaVersion)
        assertTrue(ControllerCapability.START in descriptor.capabilities)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank controller id is rejected`() {
        ControllerId(" ")
    }
}
