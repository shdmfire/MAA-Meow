package com.aliothmoon.maameow.automation.remote

import com.aliothmoon.maameow.automation.remote.internal.VirtualDisplayManager
import org.junit.Assert.assertEquals
import org.junit.Test

class ResolutionValidationTest {
    @Test
    fun `positive resolution is retained`() {
        val config = VirtualDisplayManager.DisplayConfig(1920, 1080, 240)
        assertEquals(1920, config.width)
        assertEquals(1080, config.height)
        assertEquals(240, config.dpi)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `non-positive resolution is rejected`() {
        VirtualDisplayManager.DisplayConfig(0, 720, 160)
    }
}
