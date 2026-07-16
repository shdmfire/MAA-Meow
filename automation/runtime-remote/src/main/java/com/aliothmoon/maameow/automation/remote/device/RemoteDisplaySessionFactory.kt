package com.aliothmoon.maameow.automation.remote.device

interface RemoteDisplaySessionFactory {
    fun start(mode: Int, width: Int, height: Int, dpi: Int): RemoteDisplaySession
}
