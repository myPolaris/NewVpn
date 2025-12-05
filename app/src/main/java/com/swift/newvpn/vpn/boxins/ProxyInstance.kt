package com.swift.newvpn.vpn.boxins

import android.util.Log
import com.swift.newvpn.BuildConfig
import com.swift.newvpn.model.SocksBean
import com.swift.newvpn.vpn.services.ScapeVpnInterface
import com.swift.newvpn.utils.Logs
import com.swift.newvpn.utils.NotificationHelper
import com.swift.newvpn.utils.Utils
import com.swift.newvpn.utils.runCalculate
import kotlinx.coroutines.runBlocking

class ProxyInstance(profile: SocksBean, var service: ScapeVpnInterface? = null) :
    BoxInstance(profile) {

    var notTmp = true

    var displayProfileName = NotificationHelper.genTitle(profile)

    // for TrafficLooper
    var looper: TrafficLooper? = null

    override fun buildConfig() {
        super.buildConfig()
        //
        if (notTmp) Logs.d(config.config)
        if (notTmp && BuildConfig.DEBUG) Logs.d(Utils.gson.toJson(config.trafficMap))
    }

    // only use this in temporary instance
    fun buildConfigTmp() {
        notTmp = false
        buildConfig()
    }

    override suspend fun init() {
        super.init()
    }

    override suspend fun loadConfig() {
        super.loadConfig()
    }

    override fun launch() {
        Log.e("ProxyInstance", "launch0 ${service}")
       runCatching {
           box.setAsMain()
       }.onFailure { it.printStackTrace() }
        super.launch() // start box
        Log.e("ProxyInstance", "launch1 ${service}")
        service?.serviceScope?.runCalculate {
            Log.e("ProxyInstance", "launch2 ")
            looper = service?.let { TrafficLooper(it.data, this) }
            looper?.start()
        }
    }

    override fun close() {
        super.close()
        runBlocking {
            looper?.stop()
            looper = null
        }
    }
}
