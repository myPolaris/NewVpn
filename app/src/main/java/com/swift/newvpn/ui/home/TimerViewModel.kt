package com.swift.newvpn.ui.home

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.swift.newvpn.base.BaseViewModel
import com.swift.newvpn.base.SingleLiveData
import com.swift.newvpn.utils.formatToTime
import com.swift.newvpn.utils.runCalculate
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class TimerViewModel(app: Application) : BaseViewModel(app) {
    val isRefreshNativeAd = SingleLiveData(false)

    private val connectingTime = MutableLiveData(0L)//默认30分钟
    val connectingTimeStr = MutableLiveData("00:00:00")//默认 30分钟
    private var timerJob: Job? = null
    private val threshold = 30L

    fun startTimer() {
        timerJob?.cancel() // 取消之前的计时器
        var timer = connectingTime.value ?: 0L
        updateConnectingTime(timer)
        timerJob = viewModelScope.runCalculate {
            while (true) {
                delay(1000)
                connectingTime.postValue(++timer)
                checkNativeAdNeedRefresh()
                updateConnectingTime(timer)
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        updateConnectingTime(0)
    }

    private fun updateConnectingTime(time: Long) {
        if (timerJob?.isActive == true){
            connectingTimeStr.postValue(time.formatToTime())
        }
    }

    fun onNativeAdRefreshed() {
        isRefreshNativeAd.postValue(false)
        saveLastTime()
    }

    private val lastTime = MutableLiveData(0L)

    fun checkNativeAdNeedRefresh() {
        val old = lastTime.value ?: 0
        if (System.currentTimeMillis() - old > threshold * 1000) {
            isRefreshNativeAd.postValue(true)
        }
    }

    fun saveLastTime() {
        lastTime.postValue(System.currentTimeMillis())
    }


    override fun onCleared() {
        stopTimer()
        super.onCleared()
    }
}