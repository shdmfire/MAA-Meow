package com.aliothmoon.maameow.remote.legacy

import com.aliothmoon.maameow.automation.ipc.RemoteSessionRequest
import com.aliothmoon.maameow.automation.remote.engine.RemoteControllerDescriptor
import com.aliothmoon.maameow.automation.remote.engine.RemoteControllerEngine
import com.aliothmoon.maameow.automation.remote.engine.RemoteControllerEngineFactory

class MaaLegacyRemoteControllerEngineFactory : RemoteControllerEngineFactory {
    override val descriptor = RemoteControllerDescriptor(controllerId = "maa-legacy", displayName = "MAA Legacy")
    override fun create(request: RemoteSessionRequest): RemoteControllerEngine = MaaLegacyRemoteControllerEngine()
}
