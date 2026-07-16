package com.aliothmoon.maameow.automation.api

import kotlinx.serialization.Serializable

@Serializable
data class ControllerError(
    val code: String,
    val message: String,
    val recoverable: Boolean = false,
)
