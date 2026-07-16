package com.aliothmoon.maameow.automation.remote.device

interface RemoteDisplaySession {
    val displayId: Int
    val width: Int
    val height: Int
    fun stop()
}
