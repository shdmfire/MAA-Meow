package com.aliothmoon.maameow.automation.remote.engine

import com.aliothmoon.maameow.automation.ipc.RemoteSessionRequest
import com.aliothmoon.maameow.automation.remote.device.RemoteDeviceEnvironment

interface RemoteControllerEngine {
    suspend fun prepare(request: RemoteSessionRequest, environment: RemoteDeviceEnvironment): EnginePrepareResult = EnginePrepareResult.Ready
    suspend fun start(eventSink: EngineEventSink): EngineStartResult = EngineStartResult.Started
    suspend fun stop() {}
    fun destroy() {}
}
