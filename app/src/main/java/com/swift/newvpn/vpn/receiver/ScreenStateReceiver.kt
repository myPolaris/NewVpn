package com.swift.newvpn.vpn.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.swift.newvpn.vpn.services.ScapeVpnInterface
import com.swift.newvpn.vpn.services.ScapeVpnService
import com.swift.newvpn.vpn.services.VpnState

class ScreenStateReceiver(val service: ScapeVpnInterface) : BroadcastReceiver() {
    var listenPostSpeed = true

    fun register() = apply {
        (service as? ScapeVpnService)?.registerReceiver(this, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        })
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (service.data.state == VpnState.Connected) {
            listenPostSpeed = intent.action == Intent.ACTION_SCREEN_ON
        }
    }

    fun destroy() {
        listenPostSpeed = false
        (service as? ScapeVpnService)?.unregisterReceiver(this)
    }
}