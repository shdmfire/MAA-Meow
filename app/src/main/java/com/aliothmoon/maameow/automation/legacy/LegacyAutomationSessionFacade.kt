package com.aliothmoon.maameow.automation.legacy

import com.aliothmoon.maameow.automation.api.ExecutionState
import com.aliothmoon.maameow.domain.service.MaaCompositionService
import com.aliothmoon.maameow.maa.task.MaaTaskParams
import kotlinx.coroutines.flow.StateFlow

/** Temporary bridge; controller-specific task parameters remain confined to the legacy app layer. */
class LegacyAutomationSessionFacade(
    private val compositionService: MaaCompositionService,
    stateAdapter: LegacyMaaExecutionStateAdapter,
) {
    val state: StateFlow<ExecutionState> = stateAdapter.state

    suspend fun startLegacyMaa(
        tasks: List<MaaTaskParams>,
        clientType: String,
        isScheduled: Boolean = false,
        onSessionStarted: (suspend () -> Unit)? = null,
    ): MaaCompositionService.StartResult = compositionService.start(
        tasks, clientType, isScheduled, onSessionStarted
    )

    suspend fun stop(): MaaCompositionService.StopResult = compositionService.stop()
}
