package com.aliothmoon.maameow.automation.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ControllerEventEnvelope(
    val controllerId: ControllerId,
    val eventType: String,
    val schemaVersion: Int = 1,
    val payload: JsonElement,
)
