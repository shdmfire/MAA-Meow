package com.aliothmoon.maameow.automation.remote.engine

import com.aliothmoon.maameow.automation.ipc.RemoteControllerEvent

fun interface EngineEventSink {
    fun emit(event: RemoteControllerEvent)
}
