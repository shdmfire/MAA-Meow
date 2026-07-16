package com.aliothmoon.maameow.automation.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aliothmoon.maameow.automation.remote.engine.RemoteControllerEngineFactory
import java.util.ServiceLoader
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ServiceLoaderPackagingTest {
    @Test
    fun serviceLoaderCanLoadFactories() {
        val loader = ServiceLoader.load(RemoteControllerEngineFactory::class.java)
        assertNotNull(loader)
    }
}
