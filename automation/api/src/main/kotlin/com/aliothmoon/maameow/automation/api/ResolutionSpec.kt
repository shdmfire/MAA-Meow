package com.aliothmoon.maameow.automation.api

import kotlinx.serialization.Serializable

@Serializable
data class ResolutionSpec(val width: Int, val height: Int, val dpi: Int) {
    init {
        require(width > 0 && height > 0 && dpi > 0) { "resolution values must be positive" }
    }
}
