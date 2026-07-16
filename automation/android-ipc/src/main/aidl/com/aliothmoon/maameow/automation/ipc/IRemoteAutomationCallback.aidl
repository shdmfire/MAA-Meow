package com.aliothmoon.maameow.automation.ipc;

import com.aliothmoon.maameow.automation.ipc.RemoteControllerEvent;

oneway interface IRemoteAutomationCallback {
    void onEvent(in RemoteControllerEvent event);
}
