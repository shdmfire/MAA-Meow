package com.aliothmoon.maameow.data.preferences

import com.aliothmoon.maameow.utils.JsonUtils
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Test

class ConfigBackupV1CompatibilityTest {
    private val json = JsonUtils.common

    @Test
    fun `version 1 backup fixture is readable without losing represented values`() {
        val raw = requireNotNull(javaClass.classLoader?.getResource("fixtures/config-backup-v1.json")).readText()
        val backup = json.decodeFromString<ConfigBackup>(raw)

        assertEquals(1, backup.version)
        assertEquals("P1080", backup.appSettings.backgroundResolution)
        assertEquals("false", backup.notificationSettings.sendOnError)
        assertEquals("profile-v1", backup.activeProfileId)
        assertEquals("profile-v1", backup.scheduleStrategies.single().profileId)

        val roundTrip = json.decodeFromString<ConfigBackup>(json.encodeToString(backup))
        assertEquals(backup, roundTrip)
    }
}
