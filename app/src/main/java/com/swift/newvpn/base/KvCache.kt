package com.swift.newvpn.base

import android.net.Network
import com.swift.newvpn.vpn.services.ScapeVpnInterface
import com.swift.newvpn.vpn.services.ScapeVpnService
import com.swift.newvpn.vpn.services.VpnState
import com.swift.newvpn.utils.string
import com.tencent.mmkv.MMKV

object KvCache {
    @Volatile
    var serviceState = VpnState.Idle
    var vpnService: ScapeVpnService? = null
    var serviceInterface: ScapeVpnInterface? = null
    val kv = MMKV.mmkvWithID(Constants.APP_CACHE_FILE_NAME, MMKV.MULTI_PROCESS_MODE)

    var adConfig by kv.string(CacheKey.AD_CONFIG)

    var installReferrer by kv.string(CacheKey.INSTALL_REFERRER)

    var selectedProxy by kv.string(CacheKey.SELECTED_PROXY)

    var proxyList by kv.string(CacheKey.PROXY_LIST)

    var underlyingNetwork: Network? = null
}