package com.aliothmoon.maameow.automation.remote.engine

import com.aliothmoon.maameow.automation.ipc.RemoteSessionRequest

interface RemoteControllerEngineFactory {
    val descriptor: RemoteControllerDescriptor
    fun create(request: RemoteSessionRequest): RemoteControllerEngine
}
