package com.swift.newvpn.vpn

import android.os.Build
import androidx.annotation.RequiresApi
import com.swift.newvpn.base.KvCache
import com.swift.newvpn.utils.Logs
import com.swift.newvpn.utils.NotificationHelper
import com.swift.newvpn.utils.ProxyManager
import com.swift.newvpn.utils.appScope
import com.swift.newvpn.utils.connectivityManager
import com.swift.newvpn.utils.isAndroid10
import com.swift.newvpn.utils.runCalculate
import com.swift.newvpn.utils.wifiManager
import libcore.BoxPlatformInterface
import libcore.Libcore
import libcore.NB4AInterface
import java.net.InetSocketAddress

class NativeInterface : BoxPlatformInterface, NB4AInterface {

    //  libbox interface

    override fun autoDetectInterfaceControl(fd: Int) {
        KvCache.vpnService?.protect(fd)
    }

    override fun openTun(singTunOptionsJson: String, tunPlatformOptionsJson: String): Long {
        if (KvCache.vpnService == null) {
            throw Exception("no ScapeVpnService")
        }
        return KvCache.vpnService!!.startVpn(singTunOptionsJson, tunPlatformOptionsJson).toLong()
    }

    override fun useProcFS(): Boolean {
        return isAndroid10()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun findConnectionOwner(
        ipProto: Int, srcIp: String, srcPort: Int, destIp: String, destPort: Int
    ): Int {
        return connectivityManager!!.getConnectionOwnerUid(
            ipProto, InetSocketAddress(srcIp, srcPort), InetSocketAddress(destIp, destPort)
        )
    }

    override fun packageNameByUid(uid: Int): String {
        if (uid <= 1000L) {
            return "android"
        }

        error("unknown uid $uid")
    }

    override fun uidByPackageName(packageName: String): Int {
        return 0
    }

    override fun wifiState(): String {
        val connectionInfo = wifiManager?.connectionInfo ?: return ""
        return "${connectionInfo.ssid},${connectionInfo.bssid}"
    }

    // nb4a interface

    override fun useOfficialAssets(): Boolean {
        return true
    }

    override fun selector_OnProxySelected(selectorTag: String, tag: String) {
        if (selectorTag != "proxy") {
            Logs.d("other selector: $selectorTag")
            return
        }
        Libcore.resetAllConnections(true)
        KvCache.serviceInterface?.apply {
            appScope.runCalculate {
                val id = data.proxy!!.config.profileTagMap
                    .filterValues { it == tag }.keys.firstOrNull() ?: ""
                val ent = ProxyManager.getProfile(id) ?: return@runCalculate
                // s_traffic & title
                data.proxy?.apply {
                    looper?.selectMain(id)
                    displayProfileName = NotificationHelper.genTitle(ent)
                    data.notification?.postNotificationTitle(displayProfileName)
                }
                // post binder
                data.binder.broadcast { b ->
                    b.cbSelectorUpdate(id)
                }
            }
        }
    }
}
