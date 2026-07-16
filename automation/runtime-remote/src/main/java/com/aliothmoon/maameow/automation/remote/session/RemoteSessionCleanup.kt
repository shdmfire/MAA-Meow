package com.aliothmoon.maameow.automation.remote.session

import com.aliothmoon.maameow.automation.remote.input.InputControlUtils
import com.aliothmoon.maameow.automation.remote.internal.GameAudioMuteController
import com.aliothmoon.maameow.automation.remote.internal.PowerController
import com.aliothmoon.maameow.automation.remote.third.Ln
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

class RemoteSessionCleanup {
    fun cleanup(session: RemoteSession?) {
        try {
            if (session != null) {
                runCatching { runBlocking { withTimeoutOrNull(60_000L) { session.engine.stop() } } }.onFailure { Ln.w("engine stop failed: ${it.message}") }
                runCatching { session.engine.destroy() }.onFailure { Ln.w("engine destroy failed: ${it.message}") }
                runCatching { session.display.stop() }.onFailure { Ln.w("display stop failed: ${it.message}") }
            }
        } finally {
            runCatching { InputControlUtils.setTouchCallback(null) }
            runCatching { GameAudioMuteController.restoreAll() }
            runCatching { PowerController.stopUserActivityKeepAlive() }
        }
    }
}
