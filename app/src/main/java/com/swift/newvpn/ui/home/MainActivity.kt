package com.swift.newvpn.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.activity.viewModels
import com.swift.newvpn.R
import com.swift.newvpn.base.BaseActivity
import com.swift.newvpn.base.KvCache
import com.swift.newvpn.base.NextPageEvent
import com.swift.newvpn.databinding.PageMainBinding
import com.swift.newvpn.model.SocksBean
import com.swift.newvpn.model.SpeedData
import com.swift.newvpn.ui.proxy.ProxyListActivity
import com.swift.newvpn.ui.registerVpnLauncher
import com.swift.newvpn.ui.result.ResultActivity
import com.swift.newvpn.ui.setting.SettingActivity
import com.swift.newvpn.utils.AppUtils
import com.swift.newvpn.utils.formatFileSize
import com.swift.newvpn.utils.getNationalFlagImage
import com.swift.newvpn.utils.isNotificationPermissionEnable
import com.swift.newvpn.utils.observeCompat
import com.swift.newvpn.utils.setNativeDef
import com.swift.newvpn.vpn.services.ScapeVpnConnection
import com.swift.newvpn.vpn.services.VpnState

class MainActivity : BaseActivity<PageMainBinding>() {

    companion object {
        fun start(activity: Activity) {
            Intent(activity, MainActivity::class.java).apply {
                activity.startActivity(this)
            }
        }
    }

    private val viewModel: VpnViewModel by viewModels()
    private val timerViewModel: TimerViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()

    override fun getViewBinding() = PageMainBinding.inflate(layoutInflater)

    private val vpnLauncher = registerVpnLauncher()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connection.connect(this, viewModel)
        initEvent()

    }

    override fun initUI(bind: PageMainBinding) {
        viewModel.initProxy()
        changeUiByVpnSate(KvCache.serviceState.connected)
        bind.toolbar.root.apply {
            initToolbar(true)
            setOnMenuItemClickListener {
                SettingActivity.start(this@MainActivity)
                true
            }
        }
        bind.navGroup.setNativeDef()
        bind.cardProxyBgOn.setOnClickListener {
            // 断开链接
            toggleConnect()
        }
        bind.imgOffBtn.setOnClickListener {
            // 连接VPN
            toggleConnect()
        }
        bind.cardProxyBg.setOnClickListener {
            // 选择代理
            Intent(this@MainActivity, ProxyListActivity::class.java)
                .launchForResult()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    private fun changeUiByVpnSate(isConnected: Boolean) {
        withBind {
            if (isConnected) {
                it.bgCenter.visibility = View.VISIBLE
                it.groupOn.visibility = View.VISIBLE
                it.imgOffBtn.visibility = View.GONE
                it.imgConnectionState.isSelected = true
                it.tvConnectionState.text = getString(R.string.s_connected)
            } else {
                it.bgCenter.visibility = View.INVISIBLE
                it.groupOn.visibility = View.GONE
                it.imgOffBtn.visibility = View.VISIBLE
                it.tvConnectionState.text = getString(R.string.s_not_connected)
            }
        }
    }

    private fun updateSpeedData(speedData: SpeedData?) {
        withBind {
            it.tvDownloadSpeed.text = formatFileSize(speedData?.rxRateProxy ?: 0)
            it.tvUploadSpeed.text = formatFileSize(speedData?.txRateProxy ?: 0)
        }
    }

    private fun updateProxyInfo(proxy: SocksBean?) {
        proxy?.run {
            withBind {
                val flagRes = getNationalFlagImage()
                it.imgNationalFlag.setImageResource(flagRes)
                it.imgNationalFlagOn.setImageResource(flagRes)
                it.tvProxyName.text = name
                it.tvProxyNameOn.text = name
            }
        }
    }

    private fun updateConnectingTimeStr(timeStr: String?) {
        withBind {
            it.tvConnectTime.text = timeStr ?: ""
        }
    }

    private fun showNavAd() {
        withBind { bind ->
            adViewModel.loadMainAd(this@MainActivity, bind.navGroup).takeIf {
                it
            }?.apply {
                timerViewModel.onNativeAdRefreshed()
            }
        }
    }
    //********************** Event  **********************

    private fun initEvent() {
        viewModel.let {
            it.currentProxy.observeCompat(this) { info ->
                updateProxyInfo(info)
            }
            it.speedData.observeCompat(this) { speedData ->
                updateSpeedData(speedData)
            }
            it.stateData.observeCompat(this) { state ->
                adViewModel.loadAd(this@MainActivity, state)
            }
            it.onBinderDied.observeCompat(this) { b ->
                if (b) {
                    connection.disconnect(this)
                    connection.connect(this, it)
                }
            }
        }
        timerViewModel.let {
            it.connectingTimeStr.observeCompat(this) { str ->
                updateConnectingTimeStr(str)
            }
            it.isRefreshNativeAd.observeCompat(this) { isRefresh ->
                if (isRefresh) {
                    showNavAd()
                }
            }
        }
        adViewModel.let {
            it.isShowNativeAd.observeCompat(this) {
                showNavAd()
            }
            it.event.observeCompat(this) { event ->
                if (event is NextPageEvent) {
                   goResultPage()
                }
            }
        }
        setActivityResultLauncher {
            if (it.resultCode == RESULT_OK) {
                viewModel.initProxy()
                toggleConnect()
            }
        }

        setPermissionLauncher {
            if (it) {
                toggleConnectImpl()
            }
        }
    }

    private fun goResultPage() {
        hideLoadingDialog()
        val isConnected = KvCache.serviceState.connected
        changeUiByVpnSate(isConnected)
        ResultActivity.start(this,!isConnected)
        if (isConnected) {
            timerViewModel.startTimer()
        } else {
            timerViewModel.stopTimer()
        }
    }

    private fun toggleConnect() {
        if (isNotificationPermissionEnable()) {
            toggleConnectImpl()
        } else {
            requestPermission(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun toggleConnectImpl() {
        showLoadingDialog()
        viewModel.checkVpnProfile {
            if (it) {
                val isStop = KvCache.serviceState.canStop
                adViewModel.startTimer(isStop)
                //TODO
//                if (BuildConfig.DEBUG){
//                    toggleByDebug(isStop)
//                } else {
                    toggleByRelease(isStop)
//                }
            } else {
                hideLoadingDialog()
                AppUtils.showToast(this@MainActivity, R.string.vpn_proxy_loading)
            }
        }
    }

    private fun toggleByDebug(isStop: Boolean) {
        //TODO
        if (isStop) {
            adViewModel.disconnected(
                this@MainActivity
            ) {
                debugState(VpnState.Stopped)
            }
        } else {
            debugState(VpnState.Connected)
        }
    }

    private fun toggleByRelease(isStop: Boolean) {
        if (isStop) {
            adViewModel.disconnected(
                this@MainActivity
            )
        } else {
            vpnLauncher.launch(null)
        }
    }

    //********************** vpn相关 **********************

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

    override fun onResume() {
        super.onResume()
        timerViewModel.checkNativeAdNeedRefresh()
    }

    override fun onPause() {
        timerViewModel.saveLastTime()
        super.onPause()
    }

    fun debugState(state: VpnState) {
        KvCache.serviceState = state
        viewModel.stateData.postValue(state)
    }
}