package com.swift.newvpn.utils

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Process
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.swift.newvpn.BuildConfig
import com.swift.newvpn.base.Action
import com.swift.newvpn.vpn.services.ScapeVpnConnection
import com.swift.newvpn.ui.home.MainActivity
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

    fun backProcessName() = "${BuildConfig.APPLICATION_ID}:scapeVpn"

    /**
     * 是否主进程
     */
    fun String.isMainProcesses() = this == packageName

    fun String.isBackProcesses() = this == backProcessName()

    fun exitApp() {
        Process.killProcess(Process.myPid())
        exitProcess(10)
    }

    @SuppressLint("WrongConstant")
    fun collapseStatusBar(context: Context) {
        runCatching {
            context.getSystemService("statusbar")
                .apply {
                    javaClass.getMethod("collapsePanels")
                        .invoke(this)
                }
        }
    }

    fun configureIntent(context: Context): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(
            app, MainActivity::class.java
        ).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
        PendingIntent.FLAG_IMMUTABLE
    )


    fun startService() = ContextCompat.startForegroundService(
        app, Intent(app, ScapeVpnConnection.serviceClass)
    )

    fun reloadService() =
        app.sendBroadcast(Intent(Action.RELOAD).setPackage(packageName))

    fun stopService() =
        app.sendBroadcast(Intent(Action.CLOSE).setPackage(packageName))

    private fun showToast(context: Context, res: String, flag: Int) {
        Toast.makeText(context, res, flag).show()
    }

    fun showToast(context: Context, res: String) {
        showToast(context, res, Toast.LENGTH_LONG)
    }

    fun showShortToast(context: Context, res: String) {
        showToast(context, res, Toast.LENGTH_SHORT)
    }

    fun showToast(context: Context, @StringRes resId: Int) {
        showToast(context, context.getString(resId))
    }

    fun showShortToast(context: Context, @StringRes resId: Int) {
        showShortToast(context, context.getString(resId))
    }
}