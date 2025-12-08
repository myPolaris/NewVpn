package com.swift.newvpn.ui.home

import android.app.Application
import android.view.ViewGroup
import androidx.lifecycle.viewModelScope
import com.swift.newvpn.ad.AdProxy
import com.swift.newvpn.base.BaseActivity
import com.swift.newvpn.base.BaseViewModel
import com.swift.newvpn.base.KvCache
import com.swift.newvpn.base.NextPageEvent
import com.swift.newvpn.base.SingleLiveData
import com.swift.newvpn.utils.AppUtils
import com.swift.newvpn.utils.postDelay
import com.swift.newvpn.utils.runCalculate
import com.swift.newvpn.vpn.services.VpnState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean

class AdViewModel(app: Application) : BaseViewModel(app) {
    private val isAdShow = AtomicBoolean(false)
    val isShowNativeAd = SingleLiveData(false)
    private val isNextPageCalled = AtomicBoolean(false)
    private var loadingTimer: Job? = null

    fun startTimer(isStop: Boolean) {
        loadingTimer?.cancel()
        isNextPageCalled.set(false)
        val maxTime = if (isStop) 9 else 12
        loadingTimer = viewModelScope.runCalculate {
            repeat(maxTime) {
                delay(1000)
            }
            if (isStop) {
                AppUtils.stopService()
                delay(1000)
            }
            toResultPage()
        }
    }

    fun loadAd(activity: BaseActivity<*>, state: VpnState?) {
        if (state?.connected == true) {
            // 连接成功后加载广告
            AdProxy.adConnect.load {
                if (it) {
                    showAd(activity) {
                        viewModelScope.postDelay(500) {
                            toResultPage()
                        }
                    }
                }
            }
            AdProxy.adMain.load {
                isShowNativeAd.postValue(it)
            }
            AdProxy.adResult.preload()
            AdProxy.adBack.preload()
            AdProxy.adOpen.preload()
        }
    }

    fun loadMainAd(activity: BaseActivity<*>, layout: ViewGroup) =
        AdProxy.adMain.showAd(activity, layout)

    private fun showAd(activity: BaseActivity<*>, onDismiss: () -> Unit) {
        AdProxy.adConnect.showAd(activity) {
            isAdShow.set(false)
            onDismiss.invoke()
        }.apply {
            isAdShow.set(this)
        }
    }

    fun disconnected(activity: MainActivity, block: (() -> Unit)? = null) {
        showAd(activity) {
            if (KvCache.serviceState.canStop) {
                AppUtils.stopService()
                block?.invoke()
            }
            viewModelScope.postDelay(500) {
                toResultPage()
            }
        }
    }

    private suspend fun toResultPage() {
        if (!isAdShow.get() && isNextPageCalled.compareAndSet(false, true)) {
            AdProxy.adConnect.removeCallback()
            loadingTimer?.cancel()
            sendEvent(NextPageEvent)
        }
    }

    override fun onCleared() {
        AdProxy.adConnect.removeCallback()
        AdProxy.adMain.removeCallback()
        loadingTimer?.cancel()
        super.onCleared()
    }
}