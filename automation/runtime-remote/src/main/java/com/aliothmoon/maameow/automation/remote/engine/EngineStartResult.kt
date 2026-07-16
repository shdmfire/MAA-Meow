package com.aliothmoon.maameow.automation.remote.engine

sealed class EngineStartResult {
    data object Started : EngineStartResult()
    data class Failed(val errorCode: String, val message: String? = null) : EngineStartResult()
}
