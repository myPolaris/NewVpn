package com.swift.newvpn.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.swift.newvpn.utils.runMain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

interface Event

object ShowAdEvent: Event
object NextPageEvent: Event

abstract class BaseViewModel(app: Application) : AndroidViewModel(app) {
    protected val _event = Channel<Event>()

    val event = _event.receiveAsFlow()

    override fun onCleared() {
        viewModelScope.cancel()
        super.onCleared()
    }

    suspend fun sendEvent(event: Event) {
        _event.send(event)
    }

    fun runMain(block: suspend CoroutineScope.() -> Unit) =
        viewModelScope.runMain(block)
}