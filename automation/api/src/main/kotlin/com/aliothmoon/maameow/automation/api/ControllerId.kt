package com.aliothmoon.maameow.automation.api

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class ControllerId(val value: String) {
    init {
        require(value.isNotBlank()) { "controllerId must not be blank" }
    }

    override fun toString(): String = value
}
