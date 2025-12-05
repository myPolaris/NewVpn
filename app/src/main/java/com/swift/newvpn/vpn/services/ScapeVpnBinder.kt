package com.swift.newvpn.vpn.services

import android.os.RemoteCallbackList
import com.swift.newvpn.IVpnCallback
import com.swift.newvpn.IVpnService
import com.swift.newvpn.utils.runCatchingDef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ScapeVpnBinder(private var data: VpnServiceData? = null) : IVpnService.Stub(), CoroutineScope,
    AutoCloseable {
    private val callbacks = object : RemoteCallbackList<IVpnCallback>() {
    }

    val callbackIdMap = mutableMapOf<IVpnCallback, Int>()

    override val coroutineContext = Dispatchers.Main.immediate + Job()

    override fun getState(): Int = (data?.state ?: VpnState.Idle).ordinal
    override fun getProfileName(): String = data?.proxy?.displayProfileName ?: "Idle"

    override fun registerCallback(cb: IVpnCallback, id: Int) {
        if (id == ScapeVpnConnection.CONNECTION_ID_RESTART_BG) {
            Runtime.getRuntime().exit(0)
            return
        }
        if (!callbackIdMap.contains(cb)) {
            callbacks.register(cb)
        }
        callbackIdMap[cb] = id
    }

    private val broadcastMutex = Mutex()

    suspend fun broadcast(work: (IVpnCallback) -> Unit) {
        broadcastMutex.withLock {
            runCatchingDef {
                repeat(callbacks.beginBroadcast()) { index ->
                    runCatchingDef { work(callbacks.getBroadcastItem(index)) }
                }
            }.apply {
                callbacks.finishBroadcast()
            }
        }
    }

    override fun unregisterCallback(cb: IVpnCallback) {
        callbackIdMap.remove(cb)
        callbacks.unregister(cb)
    }


    fun stateChanged(s: VpnState, msg: String?) = launch {
        val profileName = profileName
        broadcast { it.stateChanged(s.ordinal, profileName, msg) }
    }

    override fun close() {
        callbacks.kill()
        cancel()
        data = null
    }
}