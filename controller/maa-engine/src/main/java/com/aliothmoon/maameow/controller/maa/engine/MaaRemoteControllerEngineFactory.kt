package com.aliothmoon.maameow.controller.maa.engine

import com.aliothmoon.maameow.automation.ipc.RemoteSessionRequest
import com.aliothmoon.maameow.automation.remote.engine.RemoteControllerDescriptor
import com.aliothmoon.maameow.automation.remote.engine.RemoteControllerEngine
import com.aliothmoon.maameow.automation.remote.engine.RemoteControllerEngineFactory
import com.aliothmoon.maameow.controller.maa.contract.MaaControllerContract

/**
 * MAA 正式 [RemoteControllerEngineFactory]。
 * 通过 ServiceLoader 注册，提供 controllerId = "maa" 的 engine。
 */
class MaaRemoteControllerEngineFactory : RemoteControllerEngineFactory {
    override val descriptor = RemoteControllerDescriptor(
        controllerId = MaaControllerContract.CONTROLLER_ID,
        displayName = "MAA",
        schemaVersion = MaaControllerContract.SCHEMA_VERSION,
    )

    override fun create(request: RemoteSessionRequest): RemoteControllerEngine {
        return MaaRemoteControllerEngine(request)
    }
}
