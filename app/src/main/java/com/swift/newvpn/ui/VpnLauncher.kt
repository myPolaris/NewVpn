package com.swift.newvpn.ui

import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import com.swift.newvpn.R
import com.swift.newvpn.utils.AppUtils
import com.swift.newvpn.utils.Logs
import com.swift.newvpn.utils.broadcastReceiver
import com.swift.newvpn.utils.isAndroid13
import com.swift.newvpn.utils.keyguardManager

class VpnLauncher : AppCompatActivity() {
    companion object {
        fun start(context: Context) {
            context.startActivity(
                Intent(
                    context, VpnLauncher::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    private var keyguardReceiver: BroadcastReceiver? = null
    private val launcher = registerVpnLauncher()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (keyguardManager?.isKeyguardLocked == true) {
            keyguardReceiver = broadcastReceiver { _, _ -> launcher.launch(null) }
            if (isAndroid13()) {
                registerReceiver(
                    keyguardReceiver,
                    IntentFilter(Intent.ACTION_USER_PRESENT),
                    RECEIVER_EXPORTED
                )
            } else {
                registerReceiver(keyguardReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
            }
        } else launcher.launch(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        keyguardReceiver?.run {
            unregisterReceiver(this)
        }
    }
}

class VpnServiceLauncher : ActivityResultContract<Void?, Boolean>() {
    private var cachedIntent: Intent? = null

    override fun getSynchronousResult(
        context: Context,
        input: Void?,
    ): SynchronousResult<Boolean>? {
        VpnService.prepare(context)?.let { intent ->
            cachedIntent = intent
            return null
        }
        AppUtils.startService()
        return SynchronousResult(false)
    }

    override fun createIntent(context: Context, input: Void?) =
        cachedIntent!!.also { cachedIntent = null }

    override fun parseResult(resultCode: Int, intent: Intent?) =
        if (resultCode == RESULT_OK) {
            AppUtils.startService()
            false
        } else {
            Logs.e("Failed to start ScapeVpnService: $intent")
            true
        }
}

fun AppCompatActivity.registerVpnLauncher() = registerForActivityResult(VpnServiceLauncher()) {
    if (it) AppUtils.showToast(this, R.string.s_vpn_permission_denied)
}