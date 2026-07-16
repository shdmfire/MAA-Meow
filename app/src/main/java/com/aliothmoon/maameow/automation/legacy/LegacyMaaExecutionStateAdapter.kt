package com.aliothmoon.maameow.automation.legacy

import com.aliothmoon.maameow.automation.api.ExecutionState
import com.aliothmoon.maameow.domain.service.MaaCompositionService
import com.aliothmoon.maameow.domain.state.MaaExecutionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class LegacyMaaExecutionStateAdapter(
    compositionService: MaaCompositionService,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val state: StateFlow<ExecutionState> = compositionService.state
        .map { it.toExecutionState() }
        .stateIn(scope, SharingStarted.Eagerly, compositionService.state.value.toExecutionState())
}

internal fun MaaExecutionState.toExecutionState(): ExecutionState = when (this) {
    MaaExecutionState.IDLE -> ExecutionState.IDLE
    MaaExecutionState.STARTING -> ExecutionState.STARTING
    MaaExecutionState.RUNNING -> ExecutionState.RUNNING
    MaaExecutionState.STOPPING -> ExecutionState.STOPPING
    MaaExecutionState.ERROR -> ExecutionState.ERROR
}
