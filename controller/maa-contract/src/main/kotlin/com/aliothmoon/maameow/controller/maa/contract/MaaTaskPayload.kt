package com.aliothmoon.maameow.controller.maa.contract

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 向 MAA engine 追加任务的负载。
 */
@Serializable
data class MaaTaskPayload(
    @SerialName("tasks")
    val tasks: List<MaaTaskItem>,
)

@Serializable
data class MaaTaskItem(
    @SerialName("type")
    val type: String,
    @SerialName("params")
    val params: String = "{}",
)
