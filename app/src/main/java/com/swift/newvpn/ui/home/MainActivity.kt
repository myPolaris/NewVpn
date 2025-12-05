package com.swift.newvpn.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.swift.newvpn.base.BaseActivity
import com.swift.newvpn.base.KvCache
import com.swift.newvpn.databinding.PageMainBinding
import com.swift.newvpn.vpn.services.ScapeVpnConnection
import com.swift.newvpn.vpn.services.VpnState
import com.swift.newvpn.ui.registerVpnLauncher
import com.swift.newvpn.utils.ProxyManager

class MainActivity : BaseActivity<PageMainBinding>() {

    private val viewModel: MainViewModel by viewModels()

    companion object {
        fun start(activity: Activity) {
            Intent(activity, MainActivity::class.java).apply {
                activity.startActivity(this)
            }
        }
    }

    private val vpnLauncher = registerVpnLauncher()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connection.connect(this, viewModel)
        ProxyManager.setSelectedProxy("")
    }

    override fun getViewBinding() = PageMainBinding.inflate(layoutInflater)

    override fun initUI(bind: PageMainBinding) {
        bind.btn1.setOnClickListener {
            vpnLauncher.launch(null)
        }
    }

    val connection =
        ScapeVpnConnection(ScapeVpnConnection.CONNECTION_ID_MAIN_ACTIVITY_FOREGROUND, true)

    override fun onDestroy() {
        KvCache.serviceState = VpnState.Idle
        super.onDestroy()
        connection.disconnect(this)
    }

    override fun onStart() {
        connection.updateConnectionId(ScapeVpnConnection.CONNECTION_ID_MAIN_ACTIVITY_FOREGROUND)
        super.onStart()
    }

    override fun onStop() {
        connection.updateConnectionId(ScapeVpnConnection.CONNECTION_ID_MAIN_ACTIVITY_BACKGROUND)
        super.onStop()
    }

    fun debugState(state: VpnState) {
        KvCache.serviceState = state
        viewModel.stateData.postValue(state)
    }
}