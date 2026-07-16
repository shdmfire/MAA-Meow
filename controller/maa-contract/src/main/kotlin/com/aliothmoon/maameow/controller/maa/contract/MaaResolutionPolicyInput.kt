package com.aliothmoon.maameow.controller.maa.contract

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * MAA 特殊分辨率策略输入（如 YoStarEN 等）。
 * 由 engine 根据 game client type 判定，不应留在通用 [DefaultDisplayConfig]。
 */
@Serializable
data class MaaResolutionPolicyInput(
    @SerialName("client_type")
    val clientType: String,
    @SerialName("preferred_width")
    val preferredWidth: Int = 1280,
    @SerialName("preferred_height")
    val preferredHeight: Int = 720,
) {
    /**
     * 返回 MAA-specific 建议分辨率（宽高不交换），若无需特殊处理则返回 null。
     */
    fun overrideResolution(): Pair<Int, Int>? {
        return when (clientType) {
            "YoStarEN" -> Pair(1280, 720)
            "YoStarJP" -> Pair(1280, 720)
            "YoStarKR" -> Pair(1280, 720)
            "txwy" -> Pair(1280, 720)
            else -> null
        }
    }
}
