package com.aliothmoon.maameow.automation.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteNativeDriverContractTest {
    @Test
    fun `JNI upcall surface is controller neutral and stable`() {
        val methods = RemoteNativeDriver::class.java.declaredMethods.associateBy { it.name }
        assertTrue(setOf("startApp", "touchDown", "touchMove", "touchUp", "keyDown", "keyUp").all(methods::containsKey))
        assertEquals("com.aliothmoon.maameow.automation.remote.RemoteNativeDriver", RemoteNativeDriver::class.java.name)
        assertTrue(RemoteNativeDriver::class.java.declaredMethods.none { it.parameterTypes.any { type -> type.name.contains("maa", ignoreCase = true) } })
    }
}
