package com.swift.newvpn.utils

import android.annotation.SuppressLint
import android.app.Application
import android.os.Process
import com.swift.newvpn.BuildConfig
import kotlin.system.exitProcess

object AppUtils {
    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    fun processName() = when {
        isAndroid13() -> Process.myProcessName()
        isAndroid9() -> Application.getProcessName()
        else -> runCatching {
            Class.forName("android.app.ActivityThread")
                .getDeclaredMethod("currentProcessName")
                .invoke(null) as String
        }.getOrDefault(BuildConfig.APPLICATION_ID)
    }

    fun backProcessName() = "${BuildConfig.APPLICATION_ID}:ScapeVpn"

    /**
     * 是否主进程
     */
    fun String.isMainProcesses() = this == packageName

    fun String.isBackProcesses() = this == backProcessName()

    fun exitApp() {
      Process.killProcess(Process.myPid())
        exitProcess(10)
    }
}