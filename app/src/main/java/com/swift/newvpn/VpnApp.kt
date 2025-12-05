package com.swift.newvpn

import android.app.Application
import android.content.Context
import android.os.StrictMode
import com.swift.newvpn.ad.AdUtils
import com.swift.newvpn.base.KvCache
import com.swift.newvpn.base.NetworkCallback
import com.swift.newvpn.base.RemoteConfig
import com.swift.newvpn.base.VpnActivityLifecycleCallback
import com.swift.newvpn.utils.runCalculate
import com.swift.newvpn.utils.AppUtils
import com.swift.newvpn.utils.AppUtils.isBackProcesses
import com.swift.newvpn.utils.AppUtils.isMainProcesses
import com.swift.newvpn.utils.InstallReferrerUtils
import com.swift.newvpn.utils.NotificationHelper
import com.swift.newvpn.utils.SingBoxUtils
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.DEBUG_PROPERTY_NAME
import kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import libcore.Libcore

class VpnApp : Application() {
    private var appScope = MainScope()

    companion object {
        lateinit var vpnApp: VpnApp
    }

    val process: String
        get() = AppUtils.processName()
    val isMainProcess: Boolean
        get() = process.isMainProcesses()

    val isBackProcess: Boolean
        get() = process.isBackProcesses()

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        vpnApp = this
    }

    override fun onCreate() {
        super.onCreate()
        init()
        appScope.runCalculate {
            initByBackground()
        }
        if (BuildConfig.DEBUG) {
            initWithDebug()
        }
    }

    private suspend fun initByBackground() {
        InstallReferrerUtils.startConnection(this@VpnApp)
        if (isMainProcess) {
            AdUtils.initAd(this)
            NetworkCallback.start(this) {
                KvCache.underlyingNetwork = it
            }
            NotificationHelper.updateNotificationChannels()
        }
    }

    private fun init() {
        MMKV.initialize(this)
        runCatching {
            RemoteConfig.initRemoteConfig(this)
        }
        registerActivityLifecycleCallbacks(VpnActivityLifecycleCallback())
        if (isMainProcess || isBackProcess) {
            SingBoxUtils.initSingBox(this)
        }
    }

    private fun initWithDebug() {
        System.setProperty(DEBUG_PROPERTY_NAME, DEBUG_PROPERTY_VALUE_ON)
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectLeakedRegistrationObjects()
                .penaltyLog()
                .build()
        )
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        NotificationHelper.updateNotificationChannels()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Libcore.forceGc()
        appScope.cancel()
    }
}