package com.swift.newvpn

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import com.swift.newvpn.ad.AdUtils
import com.swift.newvpn.base.RemoteConfig
import com.swift.newvpn.base.VpnActivityLifecycleCallback
import com.swift.newvpn.utils.runCalculate
import com.swift.newvpn.utils.AppUtils
import com.swift.newvpn.utils.AppUtils.isBackProcesses
import com.swift.newvpn.utils.AppUtils.isMainProcesses
import com.swift.newvpn.utils.InstallReferrerUtils
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import libcore.Libcore

class VpnApp : Application(), Configuration.Provider {
    private var appScope = MainScope()
    companion object {
        lateinit var vpnApp: VpnApp
    }

    val process : String
        get() = AppUtils.processName()
    val isMainProcess: Boolean
        get() = process.isMainProcesses()

    val isBackProcess: Boolean
        get() = process.isBackProcesses()

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setDefaultProcessName("${BuildConfig.APPLICATION_ID}:newVpn").build()

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        vpnApp = this
    }

    override fun onCreate() {
        super.onCreate()
        init()
        if (isMainProcess) {
            inMainProcess()
        }
        appScope.runCalculate {
            initByBackground()
        }
        if (BuildConfig.DEBUG) {
            initWithDebug()
        }
    }

    private fun inMainProcess() {

    }

    private suspend fun initByBackground() {
        if (isMainProcess){
            AdUtils.initAd(this)
        }
        InstallReferrerUtils.startConnection(this@VpnApp)
    }

    private fun init() {
        MMKV.initialize(this)
        runCatching {
            RemoteConfig.initRemoteConfig(this)
        }
        registerActivityLifecycleCallbacks(VpnActivityLifecycleCallback())
    }

    private fun initWithDebug() {

    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Libcore.forceGc()
        appScope.cancel()
    }
}