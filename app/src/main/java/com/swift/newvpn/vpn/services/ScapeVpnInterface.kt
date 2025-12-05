package com.swift.newvpn.vpn.services

import android.app.Service
import android.app.Service.START_NOT_STICKY
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.swift.newvpn.R
import com.swift.newvpn.base.Action
import com.swift.newvpn.base.KvCache
import com.swift.newvpn.base.NetworkCallback
import com.swift.newvpn.vpn.GuardedProcessPool
import com.swift.newvpn.utils.Logs
import com.swift.newvpn.utils.NotificationHelper
import com.swift.newvpn.utils.PluginUtils
import com.swift.newvpn.utils.ProxyManager
import com.swift.newvpn.utils.connectivityManager
import com.swift.newvpn.utils.getString
import com.swift.newvpn.utils.isAndroid13
import com.swift.newvpn.utils.readableMessage
import com.swift.newvpn.utils.runCalculate
import com.swift.newvpn.utils.runMain
import com.swift.newvpn.utils.startServiceCompat
import com.swift.newvpn.vpn.boxins.ProxyInstance
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import libcore.Libcore
import java.net.UnknownHostException

interface ScapeVpnInterface {

    val serviceScope: CoroutineScope

    val data: VpnServiceData
    val tag: String
    fun createNotification(profileName: String): NotificationHelper

    fun onBind(intent: Intent): IBinder? =
        if (intent.action == Action.SERVICE) data.binder else null

    fun reload() {
        ProxyManager.getSelectedProxy() ?: stopRunner(
            false,
            R.string.s_profile_empty.getString()
        )
        val s = data.state
        when {
            s == VpnState.Stopped -> startRunner()
            s.canStop -> stopRunner(true)
            else -> Logs.w("Illegal state $s when invoking use")
        }
    }

    suspend fun startProcesses() {
        Log.e("ScapeVpnInterface", "startProcesses")
        data.proxy!!.launch()
    }

    fun startRunner() {
        this as Context
        startServiceCompat(javaClass)
    }

    fun killProcesses() {
        data.proxy?.close()
        serviceScope.runCalculate {
            NetworkCallback.stop(this)
        }
    }

    fun stopRunner(restart: Boolean = false, msg: String? = null) {
        KvCache.serviceInterface = null
        KvCache.vpnService = null

        if (data.state == VpnState.Stopping) return
        data.notification?.destroy()
        data.notification = null
        this as Service

        data.changeState(VpnState.Stopping)

        serviceScope.runMain {
            data.connectingJob?.cancelAndJoin() // ensure stop connecting first
            // we use a coroutineScope here to allow clean-up in parallel
            coroutineScope {
                killProcesses()
                val data = data
                if (data.closeReceiverRegistered) {
                    unregisterReceiver(data.receiver)
                    data.closeReceiverRegistered = false
                }
                data.proxy = null
            }

            // change the state
            data.changeState(VpnState.Stopped, msg)
            // stop the service if nothing has bound to it
            if (restart) startRunner() else {
                stopSelf()
            }
        }
    }

    fun persistStats() {
    }

    // networks
    var upstreamInterfaceName: String?

    suspend fun preInit() {
        NetworkCallback.start(this) {
            connectivityManager?.getLinkProperties(it)?.also { link ->
                KvCache.underlyingNetwork = it
                KvCache.vpnService?.updateUnderlyingNetwork()
                //
                val oldName = upstreamInterfaceName
                if (oldName != link.interfaceName) {
                    upstreamInterfaceName = link.interfaceName
                }
                if (oldName != null && upstreamInterfaceName != null && oldName != upstreamInterfaceName) {
                    Logs.d("Network changed: $oldName -> $upstreamInterfaceName")
                    Libcore.resetAllConnections(true)
                }
            }
        }
    }

    suspend fun lateInit() {
        Log.e(tag, "lateInit: ")
        data.notification?.postNotificationWakeLockStatus(false)
    }


    fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
       KvCache.serviceInterface = this

        val data = data
        if (data.state != VpnState.Stopped) return START_NOT_STICKY
        val profile = ProxyManager.getSelectedProxy()
        this as Context
        if (profile == null) { // gracefully shutdown: https://stackoverflow.com/q/47337857/2245107
            data.notification = createNotification("")
            stopRunner(false, getString(R.string.s_profile_empty))
            return START_NOT_STICKY
        }

        val proxy = ProxyInstance(profile, this)
        data.proxy = proxy
        if (!data.closeReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(Action.RELOAD)
                addAction(Intent.ACTION_SHUTDOWN)
                addAction(Action.CLOSE)
                // addAction(Action.SWITCH_WAKE_LOCK)
                addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED)
                addAction(Action.RESET_UPSTREAM_CONNECTIONS)
            }
            if (isAndroid13()) {
                registerReceiver(
                    data.receiver,
                    filter,
                    "$packageName.SERVICE",
                    null,
                    RECEIVER_EXPORTED
                )
            } else {
                registerReceiver(data.receiver, filter, "$packageName.SERVICE", null)
            }
            data.closeReceiverRegistered = true
        }

        data.changeState(VpnState.Connecting)
        serviceScope.runMain {
            runCatching {
                data.notification = createNotification(NotificationHelper.genTitle(profile))
                PluginUtils.killAll()    // clean up old processes
                preInit()
                proxy.init()
                proxy.processes = GuardedProcessPool {
                    Logs.w(it)
                    stopRunner(false, it.readableMessage)
                }
                startProcesses()
                data.changeState(VpnState.Connected)

                lateInit()
            }.onFailure {
                Log.e(tag,it.readableMessage)
                it.printStackTrace()
                when (it) {
                    is CancellationException -> {}
                    is UnknownHostException -> stopRunner(
                        false,
                        getString(R.string.s_invalid_server)
                    )

                    else -> {
                        if (it.javaClass.name.endsWith("proxyerror")) {
                            // error from golang
                            Logs.w(it.readableMessage)
                        } else {
                            Logs.w(it)
                        }
                        stopRunner(
                            false,
                            "${getString(R.string.service_failed)}: ${it.readableMessage}"
                        )
                    }
                }
            }.apply {
                data.connectingJob = null
            }
        }
        return START_NOT_STICKY
    }
}
