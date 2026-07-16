package com.aliothmoon.maameow.automation.remote.device

import android.content.Intent
import com.aliothmoon.maameow.automation.remote.frame.RemoteFrameSource

interface RemoteDeviceEnvironment {
    val display: RemoteDisplaySession
    val frameSource: RemoteFrameSource
    fun startApp(intent: Intent): Boolean
    fun touchDown(x: Int, y: Int): Boolean
    fun touchMove(x: Int, y: Int): Boolean
    fun touchUp(x: Int, y: Int): Boolean
}
