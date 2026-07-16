package com.aliothmoon.maameow.automation.ipc;

import android.content.Intent;
import android.view.Surface;
import com.aliothmoon.maameow.automation.ipc.ITouchEventCallback;
import com.aliothmoon.maameow.automation.ipc.PermissionGrantRequest;
import com.aliothmoon.maameow.automation.ipc.PermissionStateInfo;
import com.aliothmoon.maameow.automation.ipc.RemoteSessionRequest;
import com.aliothmoon.maameow.automation.ipc.RemoteSessionInfo;

interface IRemoteAutomationService {
    oneway void destroy() = 16777114;
    int pid() = 1;
    String runtimeVersion() = 2;
    PermissionStateInfo grantPermissions(in PermissionGrantRequest request) = 3;
    boolean setForcedDisplaySize(int width, int height) = 4;
    boolean clearForcedDisplaySize() = 5;
    void setMonitorSurface(in Surface surface) = 6;
    boolean setVirtualDisplayMode(int mode) = 7;
    void setVirtualDisplayResolution(int width, int height, int dpi) = 8;
    int startVirtualDisplay() = 9;
    void stopVirtualDisplay() = 10;
    oneway void touchDown(int x, int y) = 11;
    oneway void touchMove(int x, int y) = 12;
    oneway void touchUp(int x, int y) = 13;
    oneway void setDisplayPower(boolean on) = 14;
    boolean setPlayAudioOpAllowed(String packageName, boolean isAllowed) = 15;
    int isAppAlive(String packageName) = 16;
    oneway void heartbeat(int appPid) = 17;
    oneway void setTouchCallback(ITouchEventCallback callback) = 18;
    boolean startActivity(in Intent intent) = 19;
    boolean isPackageInstalled(String packageName) = 20;
    boolean isAppOnVirtualDisplay(String packageName) = 21;
    oneway void setForceFullscreenOnVirtualDisplay(boolean enabled) = 22;
    String captureFramePng(String dirPath) = 23;
    RemoteSessionInfo startSession(in RemoteSessionRequest request) = 24;
    RemoteSessionInfo stopSession(String sessionId) = 25;
}
