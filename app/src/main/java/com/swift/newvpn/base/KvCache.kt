package com.swift.newvpn.base

import com.swift.newvpn.utils.string
import com.tencent.mmkv.MMKV

object KvCache {
//    var serviceState: ServiceState
   private val kv = MMKV.mmkvWithID(Constants.APP_CACHE_FILE_NAME, MMKV.MULTI_PROCESS_MODE)

    var adConfig by kv.string(CacheKey.AD_CONFIG)

    var vpnConfig by kv.string(CacheKey.VPN_CONFIG)
    var installReferrer by kv.string(CacheKey.INSTALL_REFERRER)
}