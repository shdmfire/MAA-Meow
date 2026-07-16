package com.aliothmoon.maameow.controller.maa.engine

import com.alibaba.fastjson2.JSON
import com.aliothmoon.maameow.automation.ipc.RemoteControllerEvent
import com.aliothmoon.maameow.automation.ipc.RemoteSessionRequest
import com.aliothmoon.maameow.automation.remote.device.RemoteDeviceEnvironment
import com.aliothmoon.maameow.automation.remote.engine.EngineEventSink
import com.aliothmoon.maameow.automation.remote.engine.EnginePrepareResult
import com.aliothmoon.maameow.automation.remote.engine.EngineStartResult
import com.aliothmoon.maameow.automation.remote.engine.RemoteControllerEngine
import com.aliothmoon.maameow.automation.remote.third.Ln
import com.aliothmoon.maameow.controller.maa.contract.MaaControllerContract
import com.aliothmoon.maameow.controller.maa.engine.core.AsstMsg
import com.aliothmoon.maameow.controller.maa.engine.core.MaaInstanceOptions
import kotlinx.coroutines.delay
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * MAA 正式 [RemoteControllerEngine] 实现。
 *
 * 职责：
 * - MaaCore instance 创建/销毁
 * - SetInstanceOption
 * - AsyncConnect 与 callback
 * - AppendTask/Start/Stop
 * - MAA connection config 构建
 * - MAA version 获取
 * - JNA load 与 native crash 日志
 *
 * 不创建或停止 VirtualDisplay（由 [RemoteSessionCoordinator] 管理）。
 */
class MaaRemoteControllerEngine(
    private val request: RemoteSessionRequest,
) : RemoteControllerEngine {

    private companion object {
        private const val TAG = "MaaRemoteControllerEngine"
        private const val CONNECT_TIMEOUT_MS = 2000L
    }

    private val coreManager = MaaCoreManager
    private val maa = coreManager.maaService
    private val connectFuture = AtomicReference<CompletableFuture<Boolean>?>(null)
    private var eventSink: EngineEventSink? = null
    private var environment: RemoteDeviceEnvironment? = null

    override suspend fun prepare(
        request: RemoteSessionRequest,
        environment: RemoteDeviceEnvironment
    ): EnginePrepareResult {
        this.environment = environment
        return EnginePrepareResult.Ready
    }

    override suspend fun start(eventSink: EngineEventSink): EngineStartResult {
        this.eventSink = eventSink

        val core = coreManager.MaaContext
        if (core == null) {
            Ln.e("$TAG: MaaCore not loaded")
            return EngineStartResult.Failed("MAA_CORE_NOT_LOADED", "MaaCore native library failed to load")
        }

        // 1. Create instance
        val instanceCb = object : MaaCoreCallback.Stub() {
            override fun onCallback(msg: Int, json: String?) {
                handleCallback(msg, json)
            }
        }
        if (!maa.CreateInstance(instanceCb)) {
            return EngineStartResult.Failed("CREATE_INSTANCE_FAILED", "Failed to create MaaCore instance")
        }

        // 2. Set touch mode
        if (!maa.SetInstanceOption(MaaInstanceOptions.TOUCH_MODE, MaaInstanceOptions.ANDROID)) {
            return EngineStartResult.Failed("SET_TOUCH_MODE_FAILED", "Failed to set touch mode")
        }

        // 3. Build connection config from environment
        val env = this.environment ?: return EngineStartResult.Failed("NO_ENVIRONMENT", "prepare() not called")
        val config = MaaConnectionConfigFactory.buildConfig(env)

        // 4. AsyncConnect
        val deferred = CompletableFuture<Boolean>()
        connectFuture.set(deferred)
        maa.AsyncConnect("", "Android", config, false)
        val connected = try {
            deferred.get(CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            Ln.e("$TAG: AsyncConnect timeout or error: ${e.message}")
            false
        } finally {
            connectFuture.set(null)
        }

        if (!connected) {
            return EngineStartResult.Failed("CONNECT_FAILED", "MAA AsyncConnect failed or timed out")
        }

        return EngineStartResult.Started
    }

    override suspend fun stop() {
        Ln.i("$TAG: stop()")
        runCatching {
            if (maa.Running()) {
                maa.Stop()
                var elapsed = 0
                while (maa.Running() && elapsed < 60_000) {
                    delay(100)
                    elapsed += 100
                }
            }
        }.onFailure {
            Ln.e("$TAG: stop error: ${it.message}")
        }
    }

    override fun destroy() {
        Ln.i("$TAG: destroy()")
        runCatching {
            maa.DestroyInstance()
        }.onFailure {
            Ln.e("$TAG: destroy error: ${it.message}")
        }
    }

    /**
     * 向 engine 追加任务。
     * 由宿主（[RemoteSessionCoordinator] 上层）调用，非 [RemoteControllerEngine] 接口标准方法，
     * 但供 Phase 5 集成使用。
     */
    fun appendTask(type: String, params: String): Boolean {
        return maa.AppendTask(type, params) > 0
    }

    fun startTasks(): Boolean = maa.Start()

    fun stopTasks(): Boolean = maa.Stop()

    fun getVersion(): String = maa.GetVersion()

    private fun handleCallback(msg: Int, json: String?) {
        // 处理 AsyncConnect 回调
        if (msg == AsstMsg.AsyncCallInfo.value) {
            val deferred = connectFuture.get() ?: return
            try {
                val obj = json?.let { JSON.parseObject(it) } ?: return
                val details = obj.getJSONObject("details")
                if (details != null) {
                    val ret = details.getBooleanValue("ret", false)
                    deferred.complete(ret)
                }
            } catch (e: Exception) {
                Ln.w("$TAG: Error parsing AsyncCallInfo callback: ${e.message}")
            }
            return
        }

        // 转发所有事件到 eventSink
        eventSink?.emit(
            RemoteControllerEvent(
                controllerId = MaaControllerContract.CONTROLLER_ID,
                eventType = "maa_callback",
                payloadJson = buildString {
                    append("{\"msg\":$msg,\"json\":${json?.let { "\"${it.replace("\"", "\\\"")}\"" } ?: "null"}}")
                }
            )
        )
    }
}
