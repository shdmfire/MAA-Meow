package com.aliothmoon.maameow.automation.api

import kotlinx.serialization.Serializable

@Serializable
enum class ControllerCapability {
    START,
    STOP,
    SCHEDULE,
    BACKGROUND_DISPLAY,
    SCREEN_CAPTURE,
    INPUT_CONTROL,
}
