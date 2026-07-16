package com.aliothmoon.maameow.controller.maa.contract

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * 验证 DTO 序列化兼容性。
 * 确保添加新字段或移动 package 后旧 JSON 仍可反序列化。
 */
class MaaPayloadCompatibilityTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `MaaRequestPayload serialization roundtrip`() {
        val payload = MaaRequestPayload(
            screenResolution = ScreenResolution(width = 1280, height = 720),
            displayId = 12,
            forceStop = true,
        )
        val encoded = json.encodeToString(payload)
        val decoded = json.decodeFromString<MaaRequestPayload>(encoded)
        assertEquals(payload.screenResolution.width, decoded.screenResolution.width)
        assertEquals(payload.screenResolution.height, decoded.screenResolution.height)
        assertEquals(payload.displayId, decoded.displayId)
        assertEquals(payload.forceStop, decoded.forceStop)
    }

    @Test
    fun `MaaRequestPayload backward compat - old JSON without clientType`() {
        val oldJson = """{"library_path":"libbridge.so","screen_resolution":{"width":1280,"height":720},"display_id":12,"force_stop":true}"""
        val decoded = json.decodeFromString<MaaRequestPayload>(oldJson)
        assertEquals(1280, decoded.screenResolution.width)
        assertEquals(720, decoded.screenResolution.height)
        assertEquals(12, decoded.displayId)
        assertEquals(true, decoded.forceStop)
        assertEquals("", decoded.clientType)
        assertEquals("libbridge.so", decoded.libraryPath)
    }

    @Test
    fun `MaaRequestPayload forward compat - new JSON with extra fields`() {
        val newJson = """{"library_path":"libbridge.so","screen_resolution":{"width":1280,"height":720},"display_id":12,"force_stop":true,"client_type":"YoStarEN","extra_field":"ignored"}"""
        val decoded = json.decodeFromString<MaaRequestPayload>(newJson)
        assertEquals("YoStarEN", decoded.clientType)
    }

    @Test
    fun `MaaEventPayload serialization`() {
        val payload = MaaEventPayload(msg = 2, json = """{"taskchain":"Fight"}""")
        val encoded = json.encodeToString(payload)
        val decoded = json.decodeFromString<MaaEventPayload>(encoded)
        assertEquals(payload.msg, decoded.msg)
        assertEquals(payload.json, decoded.json)
    }

    @Test
    fun `MaaResolutionPolicyInput YoStarEN override`() {
        val input = MaaResolutionPolicyInput(clientType = "YoStarEN")
        val override = input.overrideResolution()
        assertNotNull(override)
        assertEquals(1280, override!!.first)
        assertEquals(720, override.second)
    }

    @Test
    fun `MaaResolutionPolicyInput unknown client no override`() {
        val input = MaaResolutionPolicyInput(clientType = "SomeUnknown")
        assertEquals(null, input.overrideResolution())
    }

    @Test
    fun `MaaControllerContract constants`() {
        assertEquals("maa", MaaControllerContract.CONTROLLER_ID)
        assertEquals(1, MaaControllerContract.SCHEMA_VERSION)
    }
}
