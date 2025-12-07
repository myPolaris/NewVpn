package com.swift.newvpn.ui.home

import android.app.Application
import com.swift.newvpn.IVpnService
import com.swift.newvpn.base.BaseViewModel
import com.swift.newvpn.base.KvCache
import com.swift.newvpn.base.SingleLiveData
import com.swift.newvpn.model.SocksBean
import com.swift.newvpn.model.SpeedData
import com.swift.newvpn.model.TrafficData
import com.swift.newvpn.utils.ProxyManager
import com.swift.newvpn.vpn.services.ScapeVpnConnection
import com.swift.newvpn.vpn.services.VpnState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

class VpnViewModel(app: Application) : BaseViewModel(app), ScapeVpnConnection.Callback {

    val speedData = MutableStateFlow<SpeedData?>(null)
    val stateData = SingleLiveData<VpnState?>(null)

    val onBinderDied = SingleLiveData(false)

    val currentProxy = SingleLiveData<SocksBean?>(null)

    fun initProxy() {
        currentProxy.postValue(ProxyManager.initProxy())
    }

    private fun changeState(
        state: VpnState
    ) {
        KvCache.serviceState = state
        // 服务数据更新 。 更新速度等
        stateData.postValue(state)
    }

    override fun stateChanged(state: VpnState, profileName: String?, msg: String?) {
        changeState(state)
    }

    override fun onServiceConnected(service: IVpnService) {
        changeState(
            runCatching { VpnState.entries[service.state] }.getOrDefault(VpnState.Idle)
        )
    }

    override fun onServiceDisconnected() = changeState(VpnState.Idle)

    override fun onBinderDied() {
        onBinderDied.postValue(true)
    }

    override fun cbSpeedUpdate(stats: SpeedData) {
        speedData.tryEmit(stats)
    }

    override fun cbTrafficUpdate(data: TrafficData) {

    }

    override fun cbSelectorUpdate(name: String) {
        ProxyManager.setSelectedProxy(name)
    }

    fun checkVpnProfile(block: (Boolean) -> Unit) {
        val proxy = currentProxy.value ?: ProxyManager.initProxy().apply {
            currentProxy.postValue(this)
        }
        proxy?.also {
            runMain {
                delay(200)
                block(true)
            }
        } ?: block(false)
    }
}