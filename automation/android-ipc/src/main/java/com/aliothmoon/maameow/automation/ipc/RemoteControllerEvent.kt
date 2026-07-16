package com.aliothmoon.maameow.automation.ipc

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RemoteControllerEvent(
    val controllerId: String,
    val eventType: String,
    val schemaVersion: Int = 1,
    val payloadJson: String = "{}",
) : Parcelable
