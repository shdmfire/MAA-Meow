package com.aliothmoon.maameow.automation.remote.engine

sealed class EnginePrepareResult {
    data object Ready : EnginePrepareResult()
    data class Failed(val errorCode: String, val message: String? = null) : EnginePrepareResult()
}
