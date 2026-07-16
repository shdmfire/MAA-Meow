package com.aliothmoon.maameow.automation.remote.session

import com.aliothmoon.maameow.automation.remote.device.RemoteDisplaySession
import com.aliothmoon.maameow.automation.remote.engine.RemoteControllerEngine

data class RemoteSession(
    val sessionId: String,
    val controllerId: String,
    val engine: RemoteControllerEngine,
    val display: RemoteDisplaySession,
    var state: RemoteSessionState = RemoteSessionState.STARTING,
)
