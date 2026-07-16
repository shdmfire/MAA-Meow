package com.aliothmoon.maameow.automation.remote.frame

interface FrameHandle : AutoCloseable {
    val width: Int
    val height: Int
    val format: FramePixelFormat
    val bytes: ByteArray?
    override fun close()
}
