package com.aliothmoon.maameow.automation.ipc

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RemoteSessionInfo(
    val sessionId: String = "",
    val state: String,
    val errorCode: String? = null,
    val message: String? = null,
) : Parcelable
