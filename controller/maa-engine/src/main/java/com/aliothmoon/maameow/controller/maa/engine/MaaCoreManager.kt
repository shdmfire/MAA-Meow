package com.aliothmoon.maameow.controller.maa.engine

import com.aliothmoon.maameow.automation.remote.RemoteBootTrace
import com.aliothmoon.maameow.automation.remote.third.Ln
import com.aliothmoon.maameow.controller.maa.engine.core.MaaCoreLibrary
import com.sun.jna.Native

/**
 * MAA Core 原生库加载与生命周期管理。
 * JNA 加载 MaaCore.so，持有 [MaaCoreServiceImpl] 作为 AIDL 服务外观。
 *
 * 预期运行在 Root/Shizuku 远程特权进程中，[maaService] 通过 Binder 暴露给 app 进程。
 */
object MaaCoreManager {
    private const val TAG = "MaaCoreManager"

    val MaaContext: MaaCoreLibrary? by lazy {
        runCatching {
            System.setProperty("jna.tmpdir", "/data/local/tmp")
            RemoteBootTrace.mark("MAA_LOAD_BEGIN", "jna.tmpdir=/data/local/tmp")
            Ln.i("$TAG: Loading MaaCore...")
            Native.load("MaaCore", MaaCoreLibrary::class.java).also {
                RemoteBootTrace.mark("MAA_LOAD_OK")
                Ln.i("$TAG: MaaCore loaded successfully")
            }
        }.onFailure {
            RemoteBootTrace.mark("MAA_LOAD_FAIL", "${it.javaClass.simpleName}: ${it.message}")
            Ln.e("$TAG: Failed to load MaaCore: ${it.message}")
            Ln.e(it.stackTraceToString())
        }.getOrNull()
    }

    val maaService: MaaCoreServiceImpl = MaaCoreServiceImpl(MaaContext)

    fun destroy() {
        Ln.i("$TAG: destroy()")
        maaService.DestroyInstance()
    }
}
