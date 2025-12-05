package com.swift.newvpn.base

import com.swift.newvpn.VpnApp
import com.swift.newvpn.utils.appScope
import com.swift.newvpn.utils.runCalculate
import com.swift.newvpn.utils.runCatchingDef
import com.swift.newvpn.model.ProxyEntity
import com.swift.newvpn.utils.InstallReferrerUtils
import com.swift.newvpn.utils.Utils

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

        appScope.runCalculate {
            getVpnProxyList().takeIf { c -> c.isNotEmpty() }?.runCatchingDef {
                Utils.gson.fromJson(it, ProxyEntity::class.java)
                    ?.toSocksBenList()
//                        ?.createProfiles() TODO
            }
        }

        InstallReferrerUtils.isOrganicUser.set(InstallReferrerUtils.remoteRefOrganic(true))
//            }
//        }
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
        KvCache.vpnConfig.takeIf { it.isNotEmpty() }
            ?: "ewogICJjb25mX3NlcnYiOiB7CiAgICAibGlzdHMiOiBbCiAgICAgIHsKICAgICAgICAiY291bnRyeSI6ICJVUyIsCiAgICAgICAgImFsaWFzIjogIkNhbGlmb3JuaWEtMDEiLAogICAgICAgICJzZXJ2ZXIiOiAiMS4xLjEuMSIsCiAgICAgICAgInBvcnQiOiA0NDMsCiAgICAgICAgInVzZXIiOiAieHh4eCIsCiAgICAgICAgInBhc3MiOiAieHh4IiwKICAgICAgICAicGluZyI6IDE4CiAgICAgIH0sCiAgICAgIHsKICAgICAgICAiY291bnRyeSI6ICJVUyIsCiAgICAgICAgImFsaWFzIjogIkNhbGlmb3JuaWEtMDIiLAogICAgICAgICJzZXJ2ZXIiOiAiMS4xLjEuMSIsCiAgICAgICAgInBvcnQiOiA0NDMsCiAgICAgICAgInVzZXIiOiAieHh4eCIsCiAgICAgICAgInBhc3MiOiAieHh4IiwKICAgICAgICAicGluZyI6IDE2CiAgICAgIH0KICAgIF0KICB9Cn0="
    ) {
        KvCache.vpnConfig = it
    }

    private fun getStringConfig(key: String, def: String, save: ((String) -> Unit)? = null) =
        ""// config.getString(key)
            .takeIf {
                it.isNotEmpty()
            }?.apply {
                save?.invoke(this)
            } ?: Utils.b64DecodeUrlSafe(def)
}