package com.aliothmoon.maameow.automation.app

import com.aliothmoon.maameow.automation.ipc.RemoteSessionRequest

class AutomationSessionCoordinator(
    private val client: RemoteAutomationClient,
    private val store: ActiveSessionStore,
) {
    @Synchronized fun start(request: RemoteSessionRequest): ActiveSession? {
        if (store.get() != null) return null
        val info = client.startSession(request) ?: return null
        if (info.errorCode != null || info.sessionId.isBlank()) return null
        return ActiveSession(info.sessionId, request.controllerId, info.state).also(store::set)
    }

    @Synchronized fun stop(): Boolean {
        val active = store.get() ?: return true
        client.stopSession(active.sessionId)
        store.clear()
        return true
    }
}
