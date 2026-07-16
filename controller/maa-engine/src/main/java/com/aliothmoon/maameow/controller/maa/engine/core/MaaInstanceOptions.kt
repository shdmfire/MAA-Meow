package com.aliothmoon.maameow.controller.maa.engine.core

object MaaInstanceOptions {
    const val TOUCH_MODE = 2
    const val ANDROID = "Android"
    const val DEPLOYMENT_WITH_PAUSE = 3

    /**
     * 客户端类型 (v6.9.0+ 新增)
     *
     * 仅用于 PC 端的 WSA / 应用宝模拟器, 让 Core 解析对应渠道的游戏包名。
     * MaaMeow 通过虚拟显示直接控制 Android 设备, 不需要设置该选项 (保持默认空字符串即可)。
     */
    const val CLIENT_TYPE = 6
}
