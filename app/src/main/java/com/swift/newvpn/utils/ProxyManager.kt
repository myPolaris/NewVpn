package com.swift.newvpn.utils

import com.google.gson.reflect.TypeToken
import com.swift.newvpn.base.KvCache
import com.swift.newvpn.base.RemoteConfig
import com.swift.newvpn.model.SocksBean
import java.util.concurrent.atomic.AtomicReference

private fun String.toSocksBeans(): List<SocksBean>? =
    runCatchingDef {
        val type = object : TypeToken<List<SocksBean>>() {}.type
        Utils.gson.fromJson(this, type)
    }

object ProxyManager {
    private val proxyList = AtomicReference<List<SocksBean>>()

    private val selectedProxyBean = AtomicReference<SocksBean>()

    fun initProxy() = synchronized(selectedProxyBean) {
        getSelectedProxy() ?: setSelectedProxy(KvCache.selectedProxy)
    }

    fun getSelectedProxy() = synchronized(selectedProxyBean) {
        selectedProxyBean.get() ?: getProfile(KvCache.selectedProxy).apply {
            selectedProxyBean.set(this)
        }
    }

    fun setSelectedProxy(name: String) = synchronized(selectedProxyBean) {
        getProfileDef(name).apply {
            setSelectedProxy(this)
        }
    }

    private fun getProfileDef(name: String): SocksBean? =
        name.takeIf { it.isNotEmpty() }?.run {
            getProfile(this)
        } ?: getAll().firstOrNull()

    private fun setSelectedProxy(proxy: SocksBean?) {
        selectedProxyBean.set(proxy)
        KvCache.selectedProxy = proxy?.name ?: ""
    }

    fun getProfile(name: String): SocksBean? =
        name.takeIf { it.isNotEmpty() }?.run { getAll().find { it.name == name } }

    fun getAll(): List<SocksBean> =
        synchronized(proxyList) {
            proxyList.get()//内存
                ?: KvCache.proxyList.toSocksBeans()//本地文件
                ?: RemoteConfig.getRemoteProxy()//远程配置
                ?: emptyList()
        }

    fun updateProfile(bean: SocksBean) {
        proxyList.get()?.find { it.name == bean.name }?.also {
            it.rx = bean.rx
            it.tx = bean.tx
        }
    }

    fun createProfiles(beans: List<SocksBean>) {
        synchronized(proxyList) {
            proxyList.set(beans)
            KvCache.proxyList = Utils.gson.toJson(beans)
        }
    }
}