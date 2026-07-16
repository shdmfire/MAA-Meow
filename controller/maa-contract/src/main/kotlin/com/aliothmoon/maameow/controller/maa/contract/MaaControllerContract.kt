package com.aliothmoon.maameow.controller.maa.contract

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 固定 controllerId 与 schema version。
 * 所有 DTO 使用稳定 [SerialName]，避免移动 package 后破坏旧 JSON。
 */
object MaaControllerContract {
    const val CONTROLLER_ID = "maa"
    const val SCHEMA_VERSION = 1
}
