package com.swift.newvpn.ui.open

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.swift.newvpn.ad.AdProxy
import com.swift.newvpn.base.BaseActivity
import com.swift.newvpn.base.BaseViewModel
import com.swift.newvpn.base.NextPageEvent
import com.swift.newvpn.base.ShowAdEvent
import com.swift.newvpn.utils.postDelay
import com.swift.newvpn.utils.runCalculate
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean

class OpenViewModel(app: Application) : BaseViewModel(app) {
    private var timer: Job? = null
    val maxProgress = MutableLiveData(100)
    val progress = MutableLiveData(0)
    val isAdShow = AtomicBoolean(false)
    private val isGoNext = AtomicBoolean(false)

    override fun onCleared() {
        stop()
        super.onCleared()
    }

    fun start() {
        if (timer?.isActive == true)
            return
        isAdShow.set(false)
        isGoNext.set(false)
        val maxTime = 12000
        val interval = 15L
        val totalUpdates = (maxTime / interval).toInt()
        maxProgress.postValue(totalUpdates)
        timer = viewModelScope.runCalculate {
            runCalculate {
                repeat(totalUpdates) {
                    delay(interval)
                    progress.postValue(it + 1)
                }
                if (!isAdShow.get()) {
                    goNextPage()
                }
            }
            postDelay(1000) {
                sendEvent(ShowAdEvent)
            }
        }
    }

    fun stop() {
        timer?.cancel()
        timer = null
    }

    fun showAd(activity: BaseActivity<*>) {
        AdProxy.adOpen.showAd(activity) {
            isAdShow.set(false)
            viewModelScope.runCalculate {
                delay(700L)
                timer?.cancel()
                progress.postValue(maxProgress.value ?: 100)
                goNextPage()
            }
        }.apply {
            isAdShow.set(this)
        }
    }

    private suspend fun goNextPage() {
        if (isGoNext.compareAndSet(false, true)) {
            sendEvent(NextPageEvent)
        }
    }
}