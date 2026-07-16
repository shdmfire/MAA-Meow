package com.aliothmoon.maameow.maa.callback

import com.aliothmoon.maameow.domain.service.MaaNotificationCenter
import com.aliothmoon.maameow.domain.service.MaaSessionLogger
import com.aliothmoon.maameow.domain.state.MaaExecutionState
import com.aliothmoon.maameow.maa.AsstMsg
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class MaaCallbackBehaviorContractTest {
    private val logger = mockk<MaaSessionLogger>(relaxed = true)
    private val state = mockk<MaaExecutionStateHolder>(relaxed = true)
    private val connection = mockk<ConnectionInfoHandler>(relaxed = true)
    private val taskChain = mockk<TaskChainHandler>(relaxed = true)
    private val subTask = mockk<SubTaskHandler>(relaxed = true)
    private val notifications = mockk<MaaNotificationCenter>(relaxed = true)
    private val dispatcher = MaaCallbackDispatcher(logger, state, connection, taskChain, subTask, notifications)

    @Test
    fun `init failure reports error and closes failed session`() {
        dispatcher.dispatch(AsstMsg.InitFailed.value, "{\"what\":\"load\",\"why\":\"missing\"}")
        verify { state.reportRunState(MaaExecutionState.ERROR) }
        verify { logger.endSession("INIT_FAILED") }
    }

    @Test
    fun `all tasks completed returns idle delegates event and closes session`() {
        dispatcher.dispatch(AsstMsg.AllTasksCompleted.value, "{\"taskchain\":\"Fight\"}")
        verify { state.reportRunState(MaaExecutionState.IDLE) }
        verify { taskChain.handle(AsstMsg.AllTasksCompleted, any()) }
        verify { logger.endSession("COMPLETED") }
    }

    @Test
    fun `task chain error delegates without ending global session`() {
        dispatcher.dispatch(AsstMsg.TaskChainError.value, "{\"taskchain\":\"Fight\"}")
        verify { taskChain.handle(AsstMsg.TaskChainError, any()) }
        verify(exactly = 0) { state.reportRunState(any()) }
        verify(exactly = 0) { logger.endSession(any()) }
    }

    @Test
    fun `manual stop returns idle and closes stopped session`() {
        dispatcher.dispatch(AsstMsg.TaskChainStopped.value, "{}")
        verify { state.reportRunState(MaaExecutionState.IDLE) }
        verify { taskChain.handle(AsstMsg.TaskChainStopped, any()) }
        verify { logger.endSession("STOPPED") }
    }

    @Test
    fun `destroyed returns idle and completes destroyed session`() {
        dispatcher.dispatch(AsstMsg.Destroyed.value, null)
        verify { state.reportRunState(MaaExecutionState.IDLE) }
        verify { logger.completeSession("DESTROYED", any(), any()) }
    }

    @Test
    fun `connection and subtask callbacks keep their routing`() {
        dispatcher.dispatch(AsstMsg.ConnectionInfo.value, "{}")
        dispatcher.dispatch(AsstMsg.SubTaskCompleted.value, "{}")
        verify { connection.handle(any()) }
        verify { subTask.handle(AsstMsg.SubTaskCompleted, any()) }
    }
}
