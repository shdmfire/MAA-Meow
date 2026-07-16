package com.aliothmoon.maameow.controller.maa.contract

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * MAA Core 回调事件负载，由 MaaRemoteControllerEngine 通过 EngineEventSink 发出。
 */
@Serializable
data class MaaEventPayload(
    @SerialName("msg")
    val msg: Int,
    @SerialName("json")
    val json: String? = null,
)
