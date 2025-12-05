package com.swift.newvpn.vpn.services

import android.content.Intent
import android.os.PowerManager
import android.widget.Toast
import com.swift.newvpn.base.Action
import com.swift.newvpn.base.KvCache
import com.swift.newvpn.utils.AppUtils
import com.swift.newvpn.utils.NotificationHelper
import com.swift.newvpn.utils.appScope
import com.swift.newvpn.utils.broadcastReceiver
import com.swift.newvpn.utils.powerManager
import com.swift.newvpn.utils.runCalculate
import com.swift.newvpn.utils.toMain
import com.swift.newvpn.vpn.boxins.ProxyInstance
import kotlinx.coroutines.Job
import libcore.Libcore

class VpnServiceData(private val service: ScapeVpnInterface) {
    var state = VpnState.Stopped
    var proxy: ProxyInstance? = null
    var notification: NotificationHelper? = null

    val receiver = broadcastReceiver { ctx, intent ->
        when (intent.action) {
            Intent.ACTION_SHUTDOWN -> service.persistStats()
            Action.RELOAD -> service.reload()
            // Action.SWITCH_WAKE_LOCK -> appScope.runCalculate { service.switchWakeLock() }
            PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED -> {
                if (powerManager?.isDeviceIdleMode == true) {
                    proxy?.box?.sleep()
                } else {
                    proxy?.box?.wake()
                }
            }

            Action.RESET_UPSTREAM_CONNECTIONS -> appScope.runCalculate {
                Libcore.resetAllConnections(true)
                toMain {
                    AppUtils.collapseStatusBar(ctx)
                    Toast.makeText(ctx, "Reset upstream connections done", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            else -> service.stopRunner()
        }
    }
    var closeReceiverRegistered = false

    val binder = ScapeVpnBinder(this)
    var connectingJob: Job? = null

    fun changeState(s: VpnState, msg: String? = null) {
        if (state == s && msg == null) return
        state = s
        KvCache.serviceState = s
        binder.stateChanged(s, msg)
    }
}