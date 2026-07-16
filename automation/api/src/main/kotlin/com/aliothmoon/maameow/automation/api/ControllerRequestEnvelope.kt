package com.aliothmoon.maameow.automation.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** The host transports [payload] as opaque controller-owned data and must not interpret it. */
@Serializable
data class ControllerRequestEnvelope(
    val controllerId: ControllerId,
    val requestType: String,
    val schemaVersion: Int = 1,
    val payload: JsonElement,
)
