package com.swift.newvpn.utils

import com.swift.newvpn.VpnApp
import com.swift.newvpn.vpn.Config
import com.swift.newvpn.vpn.LocalResolverImpl
import com.swift.newvpn.vpn.NativeInterface
import go.Seq
import libcore.Libcore

object SingBoxUtils {
    private val nativeInterface = NativeInterface()

    fun initSingBox(app: VpnApp) {
        val externalAssets = app.getExternalFilesDir(null) ?: app.filesDir
        externalAssets.mkdirs()
        Seq.setContext(app)
        Libcore.initCore(
            app.process,
            app.cacheDir.absolutePath + "/",
            app.filesDir.absolutePath + "/",
            externalAssets.absolutePath + "/",
            0,
            Config.logLevel > 0,
            nativeInterface, nativeInterface, LocalResolverImpl
        )

        Utils.handleWebviewDir(app)

        appScope.runCalculate {
            app.cleanWebview()
        }
    }
}