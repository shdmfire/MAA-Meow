package com.aliothmoon.maameow.controller.maa.engine

import com.aliothmoon.maameow.automation.remote.device.RemoteDeviceEnvironment
import com.aliothmoon.maameow.controller.maa.contract.MaaResolutionPolicyInput
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * 根据 [RemoteDeviceEnvironment] 和 client type 构建 MAA 连接配置 JSON。
 *
 * 生成的 JSON 格式:
 * ```json
 * {
 *   "library_path": "libbridge.so",
 *   "screen_resolution": {"width": 1280, "height": 720},
 *   "display_id": 12,
 *   "force_stop": true
 * }
 * ```
 */
object MaaConnectionConfigFactory {

    fun buildConfig(
        environment: RemoteDeviceEnvironment,
        clientType: String = "",
        forceStop: Boolean = true,
    ): String {
        val display = environment.display
        val resolution = MaaResolutionPolicyInput(clientType)
            .overrideResolution() ?: Pair(display.width, display.height)

        return buildJsonObject {
            put("library_path", "libbridge.so")
            put("screen_resolution", buildJsonObject {
                put("width", resolution.first)
                put("height", resolution.second)
            })
            put("display_id", display.displayId)
            put("force_stop", forceStop)
        }.toString()
    }
}
