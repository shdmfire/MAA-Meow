package com.aliothmoon.maameow.schedule

import com.aliothmoon.maameow.schedule.model.ScheduleStrategy
import com.aliothmoon.maameow.utils.JsonUtils
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class ScheduleStrategySerializationTest {
    private val json = JsonUtils.common

    @Test
    fun `legacy profileId schedule fixture can be read and written stably`() {
        val raw = requireNotNull(javaClass.classLoader?.getResource("fixtures/schedule-v1.json")).readText()
        val decoded = json.decodeFromString<List<ScheduleStrategy>>(raw)

        assertEquals("profile-v1", decoded.single().profileId)
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), decoded.single().daysOfWeek)
        assertEquals(listOf(LocalTime.of(8, 30), LocalTime.of(20, 5)), decoded.single().executionTimes)

        val encoded = json.encodeToString(decoded)
        assertTrue(encoded.contains("\"profileId\":\"profile-v1\""))
        assertEquals(decoded, json.decodeFromString<List<ScheduleStrategy>>(encoded))
    }
}
