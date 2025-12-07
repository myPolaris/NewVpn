package com.swift.newvpn.ui.proxy

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.swift.newvpn.base.BaseViewModel
import com.swift.newvpn.model.SocksBean
import com.swift.newvpn.utils.ProxyManager
import com.swift.newvpn.utils.runCalculate

class ProxyListViewModel(app: Application) : BaseViewModel(app) {
    val proxyList = MutableLiveData<List<SocksBean>>()

    init {
        viewModelScope.runCalculate {
            ProxyManager.getAll()
                .let {
                    proxyList.postValue(it)
                }
        }
    }
}
