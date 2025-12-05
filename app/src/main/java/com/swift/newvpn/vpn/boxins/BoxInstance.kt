package com.swift.newvpn.vpn.boxins

import android.util.Log
import com.swift.newvpn.model.SocksBean
import com.swift.newvpn.vpn.LocalResolverImpl
import com.swift.newvpn.vpn.ConfigBuildResult
import com.swift.newvpn.vpn.buildConfig
import com.swift.newvpn.vpn.GuardedProcessPool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.plus
import libcore.BoxInstance
import libcore.Libcore
import java.io.File

abstract class BoxInstance(
    val profile: SocksBean
) : AbstractInstance {

    lateinit var config: ConfigBuildResult
    lateinit var box: BoxInstance
    val externalInstances = hashMapOf<Int, AbstractInstance>()
    open lateinit var processes: GuardedProcessPool
    private var cacheFiles = ArrayList<File>()
    fun isInitialized(): Boolean {
        return ::config.isInitialized && ::box.isInitialized
    }

    protected open fun buildConfig() {
        config = buildConfig(profile)
    }

    protected open suspend fun loadConfig() {
        box = Libcore.newSingBoxInstance(config.config, LocalResolverImpl)

    }

    open suspend fun init() {
        buildConfig()
        loadConfig()
    }

    override fun launch() {
        Log.e("BoxInstance", "launch")
        box.start()
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    override fun close() {
        for (instance in externalInstances.values) {
            runCatching {
                instance.close()
            }
        }

        cacheFiles.removeAll { it.delete(); true }

        if (::processes.isInitialized) processes.close(GlobalScope + Dispatchers.IO)

        if (::box.isInitialized) {
            box.close()
        }
    }
}