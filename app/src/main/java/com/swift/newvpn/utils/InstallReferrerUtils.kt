package com.swift.newvpn.utils

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.google.gson.reflect.TypeToken
import com.swift.newvpn.base.KvCache
import com.swift.newvpn.base.RemoteConfig
import java.util.concurrent.atomic.AtomicBoolean

internal class StateCallback(val block: () -> Unit) : InstallReferrerStateListener {
    override fun onInstallReferrerSetupFinished(p0: Int) {
        block()
    }

    override fun onInstallReferrerServiceDisconnected() {
    }
}

object InstallReferrerUtils {

    var isOrganicUser = AtomicBoolean(true)
    fun startConnection(context: Context) {
        KvCache.installReferrer
            .takeIf { it.isEmpty() }?.also {
                InstallReferrerClient.newBuilder(context).build()
                    .startConnectionImpl()
            } ?: isOrganicUser.set(remoteRefOrganic())
    }

    private fun InstallReferrerClient.startConnectionImpl() {
        startConnection(StateCallback {
            runCatchingDef {
                KvCache.installReferrer =
                    installReferrer?.installReferrer ?: ""
            }
            isOrganicUser.set(remoteRefOrganic())
            runCatchingDef { endConnection() }
        })
    }

    private val arrayList = arrayListOf<String>()

    fun checkUrlContainLists(installRef: String): Boolean {
        return arrayList.find {
            installRef.contains(it)
        } != null
    }

    /**
     * 检查当前用户是否是自然量用户
     * */
    fun remoteRefOrganic(isClear: Boolean = false): Boolean {
        val installRef = KvCache.installReferrer
        if (isClear) {
            arrayList.clear()
        }
        when {
            installRef.isEmpty() -> return true
            arrayList.isNotEmpty() -> return checkUrlContainLists(installRef)
        }

        if (handlerRemoteConfig()) {
            return checkUrlContainLists(installRef)
        }

        return false
    }

    private fun handlerRemoteConfig() = RemoteConfig.getOrganic().let { config ->
        runCatchingDef {
            val type = object : TypeToken<List<String>>() {}.type
            Utils.gson.fromJson<List<String>>(config, type)
        }?.takeIf { l -> l.isNotEmpty() }
            ?.run {
                arrayList.clear()
                arrayList.addAll(this)
            } ?: false
    }
}