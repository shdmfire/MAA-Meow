package com.aliothmoon.maameow.automation.api

import kotlinx.serialization.Serializable

@Serializable
data class ScheduledControllerRequest(
    val controllerId: ControllerId,
    val request: ControllerRequestEnvelope,
    val scheduledAtEpochMillis: Long,
    val forceStart: Boolean = false,
)
