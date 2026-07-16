package com.aliothmoon.maameow.automation.remote.frame

fun interface RemoteFrameSource {
    fun acquireLatestFrame(): FrameHandle?
}
