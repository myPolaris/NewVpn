package com.swift.newvpn.base

import com.swift.newvpn.VpnApp
import com.swift.newvpn.utils.appScope
import com.swift.newvpn.utils.runCalculate
import com.swift.newvpn.utils.runCatchingDef
import com.swift.newvpn.model.ProxyEntity
import com.swift.newvpn.utils.InstallReferrerUtils
import com.swift.newvpn.utils.ProxyManager
import com.swift.newvpn.utils.Utils
import com.swift.newvpn.utils.app

object RemoteConfig {

//    private lateinit var remoteConfig: FirebaseRemoteConfig

    fun initRemoteConfig(app: VpnApp) {
//        FirebaseApp.initializeApp(app)
//        remoteConfig = Firebase.remoteConfig
        if (app.isMainProcess) {
            getRemoteConfig()
        }
    }

    private fun getRemoteConfig() {
//        remoteConfig.fetchAndActivate().addOnCompleteListener {
//            if (it.isSuccessful) {
        getAdConfig().takeIf {
            it.isNotEmpty()
        }?.let {
            KvCache.adConfig = Utils.b64EncodeUrlSafe(it)
        }

        getRemoteProxy()

        InstallReferrerUtils.checkOrganic(true)
//            }
//        }
    }

    fun getRemoteProxy() = getVpnProxyList().takeIf { it.isNotEmpty() }?.runCatchingDef {
        Utils.gson.fromJson(it, ProxyEntity::class.java)
            ?.toSocksBenList()?.apply {
                ProxyManager.createProfiles(this)
            }
    }

    fun getOrganic(): String = getStringConfig(
        "scape_conf_us",
        "WyJub3QyMCVzZXQiLCJvcmdhbmljIl0="
    )

    private fun getAdConfig(): String =
        getStringConfig(
            "scape_conf_ad",
            "ewogICJhZF9jb25mIjogewogICAgImFkX3NwbGFzaCI6IHsKICAgICAgInR5cGUiOiAib3AiLAogICAgICAiaWQiOiAiY2EtYXBwLXB1Yi0zOTQwMjU2MDk5OTQyNTQ0LzkyNTczOTU5MjEiLAogICAgICAiZW5hYmxlIjogdHJ1ZQogICAgfSwKICAgICJhZF9jb25uIjogewogICAgICAidHlwZSI6ICJpdCIsCiAgICAgICJpZCI6ICJjYS1hcHAtcHViLTM5NDAyNTYwOTk5NDI1NDQvMTAzMzE3MzcxMiIsCiAgICAgICJlbmFibGUiOiB0cnVlCiAgICB9LAogICAgImFkX2JhY2siOiB7CiAgICAgICJ0eXBlIjogIml0IiwKICAgICAgImlkIjogImNhLWFwcC1wdWItMzk0MDI1NjA5OTk0MjU0NC8xMDMzMTczNzEyIiwKICAgICAgImVuYWJsZSI6IHRydWUKICAgIH0sCiAgICAibmF2X21haW4iOiB7CiAgICAgICJ0eXBlIjogIm52IiwKICAgICAgImlkIjogImNhLWFwcC1wdWItMzk0MDI1NjA5OTk0MjU0NC8yMjQ3Njk2MTEwIiwKICAgICAgImVuYWJsZSI6IHRydWUKICAgIH0sCiAgICAibmF2X3Jlc3VsdCI6IHsKICAgICAgInR5cGUiOiAibnYiLAogICAgICAiaWQiOiAiY2EtYXBwLXB1Yi0zOTQwMjU2MDk5OTQyNTQ0LzIyNDc2OTYxMTAiLAogICAgICAiZW5hYmxlIjogdHJ1ZQogICAgfQogIH0KfQ=="
        )

    fun getVpnProxyList(): String = getStringConfig(
        "scape_conf_serv",
        ScapeLib.core(app)
    )

    private fun getStringConfig(key: String, def: String) =
        ""// config.getString(key)
            .takeIf {
                it.isNotEmpty()
            } ?: Utils.b64DecodeUrlSafe(def)
}