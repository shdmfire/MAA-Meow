package com.aliothmoon.maameow.automation.api

import kotlinx.serialization.Serializable

@Serializable
enum class ExecutionState {
    IDLE,
    PREPARING,
    STARTING,
    RUNNING,
    STOPPING,
    ERROR,
}
