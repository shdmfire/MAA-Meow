package com.aliothmoon.maameow.domain.models

import com.aliothmoon.maameow.automation.api.RunMode as AutomationRunMode
import com.aliothmoon.maameow.constant.DisplayMode

enum class RunMode(
    val displayMode: Int
) {
    FOREGROUND(DisplayMode.PRIMARY),

    BACKGROUND(DisplayMode.BACKGROUND);

    fun toAutomationRunMode(): AutomationRunMode = when (this) {
        FOREGROUND -> AutomationRunMode.FOREGROUND
        BACKGROUND -> AutomationRunMode.BACKGROUND
    }

    companion object {
        fun fromAutomationRunMode(mode: AutomationRunMode): RunMode = when (mode) {
            AutomationRunMode.FOREGROUND -> FOREGROUND
            AutomationRunMode.BACKGROUND -> BACKGROUND
        }
    }
}
