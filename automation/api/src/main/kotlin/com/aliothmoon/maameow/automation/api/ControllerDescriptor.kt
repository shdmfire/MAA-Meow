package com.aliothmoon.maameow.automation.api

import kotlinx.serialization.Serializable

@Serializable
data class ControllerDescriptor(
    val controllerId: ControllerId,
    val displayName: String,
    val capabilities: Set<ControllerCapability> = emptySet(),
    val schemaVersion: Int = 1,
) {
    init {
        require(displayName.isNotBlank()) { "displayName must not be blank" }
        require(schemaVersion > 0) { "schemaVersion must be positive" }
    }
}
