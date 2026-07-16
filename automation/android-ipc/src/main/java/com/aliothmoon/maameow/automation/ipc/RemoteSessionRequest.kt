package com.aliothmoon.maameow.automation.ipc

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RemoteSessionRequest(
    val controllerId: String,
    val requestType: String,
    val schemaVersion: Int = 1,
    val payloadJson: String = "{}",
) : Parcelable
