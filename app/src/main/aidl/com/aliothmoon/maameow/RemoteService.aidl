package com.aliothmoon.maameow;

import com.aliothmoon.maameow.controller.maa.engine.MaaCoreService;
import com.aliothmoon.maameow.automation.ipc.ITouchEventCallback;
import com.aliothmoon.maameow.automation.ipc.PermissionGrantRequest;
import com.aliothmoon.maameow.automation.ipc.PermissionStateInfo;
import android.content.Intent;
import android.view.Surface;


interface RemoteService {
    oneway void destroy();
    oneway void exit();

    MaaCoreService getMaaCoreService();

    String version();
    int pid();

    boolean setup(String userDir, boolean isDebug);
    void test(in Map map);

    void screencap(int width, int height);
    String captureFramePng(String dirPath);

    boolean setForcedDisplaySize(int width, int height);
    boolean clearForcedDisplaySize();

    PermissionStateInfo grantPermissions(
        in PermissionGrantRequest request
    );

    void setMonitorSurface(in Surface surface);
    void setTouchCallback(ITouchEventCallback callback);

    void touchDown(int x, int y);
    void touchMove(int x, int y);
    void touchUp(int x, int y);

    void setDisplayPower(boolean on);

    int startVirtualDisplay();
    void stopVirtualDisplay();

    boolean setPlayAudioOpAllowed(String packageName, boolean isAllowed);

    int isAppAlive(String packageName);

    void heartbeat(int pid);

    boolean isAppOnVirtualDisplay(String packageName);
    boolean isPackageInstalled(String packageName);

    boolean startActivity(in Intent intent);

    void setForceFullscreenOnVirtualDisplay(boolean enabled);
    void setVirtualDisplayResolution(int width, int height, int dpi);
    boolean setVirtualDisplayMode(int mode);
}
