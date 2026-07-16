package com.aliothmoon.maameow.automation.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aliothmoon.maameow.automation.remote.bridge.NativeBridgeLib
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NativeBridgeLoadTest {
    @Test
    fun nativeBridgeWrapperCanBeLoadedWithoutMaaClasses() {
        assertNotNull(NativeBridgeLib::class.java)
    }
}
