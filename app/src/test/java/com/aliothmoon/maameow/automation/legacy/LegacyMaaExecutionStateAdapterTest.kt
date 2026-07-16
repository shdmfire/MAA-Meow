package com.aliothmoon.maameow.automation.legacy

import com.aliothmoon.maameow.automation.api.ExecutionState
import com.aliothmoon.maameow.domain.state.MaaExecutionState
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyMaaExecutionStateAdapterTest {
    @Test
    fun `all legacy execution states have stable generic mappings`() {
        val expected = mapOf(
            MaaExecutionState.IDLE to ExecutionState.IDLE,
            MaaExecutionState.STARTING to ExecutionState.STARTING,
            MaaExecutionState.RUNNING to ExecutionState.RUNNING,
            MaaExecutionState.STOPPING to ExecutionState.STOPPING,
            MaaExecutionState.ERROR to ExecutionState.ERROR,
        )

        assertEquals(MaaExecutionState.entries.toSet(), expected.keys)
        expected.forEach { (legacy, generic) ->
            assertEquals(generic, legacy.toExecutionState())
        }
    }
}
