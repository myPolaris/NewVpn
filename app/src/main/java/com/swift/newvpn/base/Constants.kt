package com.swift.newvpn.base

object Constants {
    const val APP_CACHE_FILE_NAME = "cleaner_cache"
    const val NOTIFICATION_CHANNEL = "scape_vpn"
    const val NOTIFICATION_NAME = "VPN Service"
}

object CacheKey{
    const val MIXED_PORT = "mixed_port"
    const val SELECTED_PROXY = "selected_proxy"
    const val VPN_CONFIG= "vpn_configuration"
    const val AD_CONFIG = "ad_configuration"
    const val INSTALL_REFERRER = "install_referrer"
    const val PROXY_LIST = "proxy_list"
}


object ExtraKey{
    const val HOT_LAUNCHER = "HOT_LAUNCHER"
}


object Action {
    const val SERVICE = "com.swift.newvpn.SERVICE"
    const val CLOSE = "com.swift.newvpn.CLOSE"
    const val RELOAD = "com.swift.newvpn.RELOAD"
    const val RESET_UPSTREAM_CONNECTIONS = "com.swift.newvpn.RESET_UPSTREAM_CONNECTIONS"
}


object TunImplementation {
    const val GVISOR = 0
    const val SYSTEM = 1
    const val MIXED = 2
}

object IPv6Mode {
    const val DISABLE = 0
    const val ENABLE = 1
    const val PREFER = 2
    const val ONLY = 3
}