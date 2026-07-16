package com.aliothmoon.maameow.constant

import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultDisplayConfigTest {
    @Test
    fun `720p and 1080p constants remain stable and 16 by 9`() {
        assertEquals(DefaultDisplayConfig.Resolution(1280, 720, 160), DefaultDisplayConfig.RES_720P)
        assertEquals(DefaultDisplayConfig.Resolution(1920, 1080, 240), DefaultDisplayConfig.RES_1080P)
        assertEquals(16f / 9f, DefaultDisplayConfig.ASPECT_RATIO, 0.0001f)
    }

    @Test
    fun `normal clients use selected preference`() {
        assertEquals(DefaultDisplayConfig.RES_720P, DefaultDisplayConfig.resolveResolution("Official", DefaultDisplayConfig.ResolutionPreference.P720))
        assertEquals(DefaultDisplayConfig.RES_1080P, DefaultDisplayConfig.resolveResolution("Official", DefaultDisplayConfig.ResolutionPreference.P1080))
    }

    @Test
    fun `YoStarEN is always 1080p`() {
        assertEquals(DefaultDisplayConfig.RES_1080P, DefaultDisplayConfig.resolveResolution("YoStarEN", DefaultDisplayConfig.ResolutionPreference.P720))
    }

    @Test
    fun `invalid persisted preference falls back to current 720p default`() {
        val preference = runCatching {
            enumValueOf<DefaultDisplayConfig.ResolutionPreference>("invalid")
        }.getOrDefault(DefaultDisplayConfig.ResolutionPreference.P720)
        assertEquals(DefaultDisplayConfig.RES_720P, DefaultDisplayConfig.resolveResolution("Official", preference))
    }
}
