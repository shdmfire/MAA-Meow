package com.aliothmoon.maameow.controller.maa.engine

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MaaCoreVersionTest {

    @Test
    fun meetsMinimumRequired_basicComparison() {
        assertTrue(MaaCoreVersion.meetsMinimumRequired("v6.0.0"))
        assertTrue(MaaCoreVersion.meetsMinimumRequired("v6.12.0"))
    }

    @Test
    fun meetsMinimumRequired_noRequirement_passes() {
        assertTrue(MaaCoreVersion.meetsMinimumRequired(null))
        assertTrue(MaaCoreVersion.meetsMinimumRequired(""))
        assertTrue(MaaCoreVersion.meetsMinimumRequired("  "))
    }

    @Test
    fun meetsMinimumRequired_malformedOrPartialVersions_areLenient() {
        assertTrue(MaaCoreVersion.meetsMinimumRequired("garbage"))
        assertTrue(MaaCoreVersion.meetsMinimumRequired("v6.12"))
    }
}
