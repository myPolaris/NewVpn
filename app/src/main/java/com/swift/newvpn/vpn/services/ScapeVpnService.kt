package com.swift.newvpn.vpn.services

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.swift.newvpn.R
import com.swift.newvpn.base.Constants.NOTIFICATION_CHANNEL
import com.swift.newvpn.base.KvCache
import com.swift.newvpn.ui.VpnLauncher
import com.swift.newvpn.utils.AppUtils
import com.swift.newvpn.utils.NotificationHelper
import com.swift.newvpn.utils.isAndroid10
import com.swift.newvpn.vpn.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class ScapeVpnService : VpnService(),ScapeVpnInterface {

    companion object {
        const val PRIVATE_VLAN4_CLIENT = "172.19.0.1"
        const val PRIVATE_VLAN4_ROUTER = "172.19.0.2"
        const val PRIVATE_VLAN6_CLIENT = "fdfe:dcba:9876::1"
    }

    override val serviceScope: CoroutineScope
        get() = MainScope()

    var conn: ParcelFileDescriptor? = null

    private var metered = false

    override var upstreamInterfaceName: String? = null

    override suspend fun startProcesses() {
        KvCache.vpnService = this
        super.startProcesses() // launch proxy instance
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    override fun killProcesses() {
        conn?.close()
        conn = null
        super.killProcesses()
    }

    override fun onBind(intent: Intent) = when (intent.action) {
        SERVICE_INTERFACE -> super<VpnService>.onBind(intent)
        else -> super<ScapeVpnInterface>.onBind(intent)
    }

    override val data = VpnServiceData(this)
    override val tag = "ScapeVpnService"

    override fun createNotification(profileName: String) =
        NotificationHelper(this, profileName, NOTIFICATION_CHANNEL)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (prepare(this) != null) {
            VpnLauncher.start(this)
        } else return super<ScapeVpnInterface>.onStartCommand(intent, flags, startId)
        stopRunner()
        return START_NOT_STICKY
    }

    inner class NullConnectionException : NullPointerException(),ExpectedException {
        override fun getLocalizedMessage() = getString(R.string.s_reboot_required)
    }

    fun startVpn(optJson: String, plat: String): Int {
        Log.e(tag,"startVpn $optJson $plat")
        Builder().setConfigureIntent(AppUtils.configureIntent(this))
            .setSession(getString(R.string.app_name))
            .setMtu(Config.mtu)
            .apply {

                // address
                addAddress(PRIVATE_VLAN4_CLIENT, 30)
                addDnsServer(PRIVATE_VLAN4_ROUTER)

                addRoute("0.0.0.0", 0)

                updateUnderlyingNetwork(this)
                if (isAndroid10()) setMetered(metered)

                metered = false
                if (isAndroid10()) setMetered(metered)
                conn = establish() ?: throw NullConnectionException()
            }
        return conn!!.fd
    }

    fun updateUnderlyingNetwork(builder: Builder? = null) {
        KvCache.underlyingNetwork?.let {
            builder?.setUnderlyingNetworks(arrayOf(KvCache.underlyingNetwork))
                ?: setUnderlyingNetworks(arrayOf(KvCache.underlyingNetwork))
        }
    }

    override fun onRevoke() = stopRunner()

    override fun onDestroy() {
        KvCache.vpnService = null
        serviceScope.cancel()
        super.onDestroy()
        data.binder.close()
    }
}