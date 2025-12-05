package com.swift.newvpn.vpn.services

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import com.swift.newvpn.IVpnCallback
import com.swift.newvpn.IVpnService
import com.swift.newvpn.base.Action
import com.swift.newvpn.base.KvCache
import com.swift.newvpn.model.SpeedData
import com.swift.newvpn.model.TrafficData
import com.swift.newvpn.utils.appScope
import com.swift.newvpn.utils.runMain
import kotlin.also
import kotlin.jvm.java

class ScapeVpnConnection(
    private var connectionId: Int,
    private var listenForDeath: Boolean = false
) : ServiceConnection, IBinder.DeathRecipient {

    companion object {
        val serviceClass= ScapeVpnService::class.java

        const val CONNECTION_ID_MAIN_ACTIVITY_FOREGROUND = 2
        const val CONNECTION_ID_MAIN_ACTIVITY_BACKGROUND = 3
        const val CONNECTION_ID_RESTART_BG = 4

        var restartingApp = false
    }

    interface Callback {

        fun cbSpeedUpdate(stats: SpeedData) {}
        fun cbTrafficUpdate(data: TrafficData) {}
        fun cbSelectorUpdate(id: String) {}

        fun stateChanged(state: VpnState, profileName: String?, msg: String?)

        fun onServiceConnected(service: IVpnService)

        /**
         * Different from Android framework, this method will be called even when you call `detachService`.
         */
        fun onServiceDisconnected() {}
        fun onBinderDied() {}
    }

    private var connectionActive = false
    private var callbackRegistered = false
    private var callback: Callback? = null
    private val serviceCallback = object : IVpnCallback.Stub() {

        override fun stateChanged(state: Int, profileName: String?, msg: String?) {
            if (state < 0) return // skip private
            val s = VpnState.entries[state]
            KvCache.serviceState = s
            val callback = callback ?: return
            appScope.runMain {
                callback.stateChanged(s, profileName, msg)
            }
        }

        override fun cbSpeedUpdate(stats: SpeedData) {
            val callback = callback ?: return
            appScope.runMain {
                callback.cbSpeedUpdate(stats)
            }
        }

        override fun cbTrafficUpdate(stats: TrafficData) {
            val callback = callback ?: return
            appScope.runMain {
                callback.cbTrafficUpdate(stats)
            }
        }

        override fun cbSelectorUpdate(id: String) {
            val callback = callback ?: return
            appScope.runMain {
                callback.cbSelectorUpdate(id)
            }
        }
    }

    private var binder: IBinder? = null

    var service: IVpnService? = null

    fun updateConnectionId(id: Int) {
        connectionId = id
        try {
            service?.registerCallback(serviceCallback, id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onServiceConnected(name: ComponentName?, binder: IBinder) {
        this.binder = binder
        val service = IVpnService.Stub.asInterface(binder)!!
        this.service = service
        try {
            if (listenForDeath) binder.linkToDeath(this, 0)
            check(!callbackRegistered)
            service.registerCallback(serviceCallback, connectionId)
            callbackRegistered = true
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        callback?.onServiceConnected(service)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        unregisterCallback()
        callback?.onServiceDisconnected()
        service = null
        binder = null
    }

    override fun binderDied() {
        service = null
        callbackRegistered = false
        if (!restartingApp) {
            callback?.also { appScope.runMain { it.onBinderDied() } }
        }
    }

    private fun unregisterCallback() {
        val service = service
        if (service != null && callbackRegistered) try {
            service.unregisterCallback(serviceCallback)
        } catch (_: RemoteException) {
        }
        callbackRegistered = false
    }

    fun connect(context: Context, callback: Callback?) {
        if (connectionActive) return
        connectionActive = true
        check(this.callback == null)
        this.callback = callback
        val intent = Intent(context, serviceClass).setAction(Action.SERVICE)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    fun disconnect(context: Context) {
        unregisterCallback()
        if (connectionActive) try {
            context.unbindService(this)
        } catch (_: IllegalArgumentException) {
        }   // ignore
        connectionActive = false
        if (listenForDeath) try {
            binder?.unlinkToDeath(this, 0)
        } catch (_: NoSuchElementException) {
        }
        binder = null
        service = null
        callback = null
    }
}