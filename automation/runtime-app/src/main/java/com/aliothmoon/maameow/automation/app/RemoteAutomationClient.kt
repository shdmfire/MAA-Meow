package com.aliothmoon.maameow.automation.app

import com.aliothmoon.maameow.automation.ipc.IRemoteAutomationService
import com.aliothmoon.maameow.automation.ipc.RemoteSessionRequest

class RemoteAutomationClient(private val serviceProvider: () -> IRemoteAutomationService?) {
    fun installedControllerIds(): List<String> = serviceProvider()?.installedControllerIds()?.toList().orEmpty()
    fun startSession(request: RemoteSessionRequest) = serviceProvider()?.startSession(request)
    fun stopSession(sessionId: String) = serviceProvider()?.stopSession(sessionId)
    fun getActiveSession() = serviceProvider()?.getActiveSession()
}
