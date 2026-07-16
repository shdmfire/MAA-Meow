package com.aliothmoon.maameow.controller.maa.contract

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * MAA 远程引擎的连接请求负载。
 */
@Serializable
data class MaaRequestPayload(
    @SerialName("library_path")
    val libraryPath: String = "libbridge.so",
    @SerialName("screen_resolution")
    val screenResolution: ScreenResolution,
    @SerialName("display_id")
    val displayId: Int,
    @SerialName("force_stop")
    val forceStop: Boolean = true,
    @SerialName("client_type")
    val clientType: String = "",
)

@Serializable
data class ScreenResolution(
    val width: Int,
    val height: Int,
)
