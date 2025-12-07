package com.swift.newvpn.ad

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import com.google.android.gms.ads.MobileAds
import com.swift.newvpn.BuildConfig
import com.swift.newvpn.base.KvCache
import com.swift.newvpn.model.AdConfig
import com.swift.newvpn.model.RemoteAdConfig
import com.swift.newvpn.utils.DeviceUtils
import com.swift.newvpn.utils.InstallReferrerUtils
import com.swift.newvpn.utils.Utils
import com.swift.newvpn.utils.runCatchingDef
import java.util.concurrent.atomic.AtomicBoolean

object AdUtils {

    private val isInitialized = AtomicBoolean(false)

    @WorkerThread
    fun initAd(context: Context) {
        if (isInitialized.compareAndSet(false, true)) {
            runCatching {
                MobileAds.initialize(context)
            }.onFailure { it.printStackTrace() }
        }
    }

    private val passAd = emptyList<String>() //listOf(AdPosition.InsBack.key)

    fun isEnable(adPos: String): Boolean {
        return BuildConfig.DEBUG || (loadEnabled() && !isPass(adPos))
    }

    fun isNativeShowEnable(adPos: String): Boolean {
        return BuildConfig.DEBUG || (DeviceUtils.hasSim() && !DeviceUtils.isDeveloper() && !isPass(
            adPos
        ))
    }

    private fun loadEnabled() =
        KvCache.serviceState.connected && DeviceUtils.hasSim() && !DeviceUtils.isDeveloper()

    private fun isPass(adPos: String): Boolean {
        return InstallReferrerUtils.isOrganicUser.get() && passAd.contains(adPos)
    }

    private var remoteAdConfig: RemoteAdConfig? = null

    fun getAdConfig(adPos: String): AdConfig? {
        return getRemoteAdConfig()?.adConfigs
            ?.takeIf { it.isNotEmpty() }
            ?.get(adPos)
            ?.also {
                Log.e("AdsLoader", "getAdConfig: $it")
                it.adPos = adPos
            }
    }

    fun getRemoteAdConfig(): RemoteAdConfig? =
        remoteAdConfig ?: KvCache.adConfig.takeIf {
            it.isNotEmpty()
        }?.runCatchingDef {
            Utils.b64DecodeUrlSafe(it)
        }?.runCatchingDef {
            Log.e("AdsLoader", "getRemoteAdConfig: $it")
            Utils.gson.fromJson(it, RemoteAdConfig::class.java)
        }?.apply {
            Log.e("AdsLoader", "getRemoteAdConfig1: $this")
            remoteAdConfig = this
        }
}