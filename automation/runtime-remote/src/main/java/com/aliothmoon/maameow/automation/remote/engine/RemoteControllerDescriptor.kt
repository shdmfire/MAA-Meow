package com.aliothmoon.maameow.automation.remote.engine

data class RemoteControllerDescriptor(
    val controllerId: String,
    val displayName: String = controllerId,
    val schemaVersion: Int = 1,
)
