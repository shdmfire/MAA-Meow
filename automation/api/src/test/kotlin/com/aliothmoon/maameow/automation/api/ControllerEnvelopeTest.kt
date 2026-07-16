package com.aliothmoon.maameow.automation.api

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Test

class ControllerEnvelopeTest {
    private val json = Json

    @Test
    fun `request envelope preserves routing fields and opaque payload`() {
        val payload = buildJsonObject {
            put("controllerOwnedField", "unchanged")
            put("nestedVersion", 7)
        }
        val envelope = ControllerRequestEnvelope(
            controllerId = ControllerId("maa"),
            requestType = "start",
            schemaVersion = 3,
            payload = payload,
        )

        val encoded = json.encodeToString(ControllerRequestEnvelope.serializer(), envelope)
        val decoded = json.decodeFromString(ControllerRequestEnvelope.serializer(), encoded)

        assertEquals(envelope, decoded)
        assertEquals("maa", decoded.controllerId.value)
        assertEquals("start", decoded.requestType)
        assertEquals(3, decoded.schemaVersion)
        assertEquals("unchanged", decoded.payload.jsonObject["controllerOwnedField"]?.jsonPrimitive?.content)
        assertEquals(payload, decoded.payload)
    }
}
