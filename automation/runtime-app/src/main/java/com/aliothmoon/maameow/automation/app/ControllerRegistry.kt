package com.aliothmoon.maameow.automation.app

class ControllerRegistry(private val idsProvider: () -> List<String> = { emptyList() }) {
    fun installedControllerIds(): List<String> = idsProvider()
}
