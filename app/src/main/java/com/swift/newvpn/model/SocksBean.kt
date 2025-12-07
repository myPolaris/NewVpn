package com.swift.newvpn.model

import android.os.Parcelable
import com.swift.newvpn.utils.unwrapIPV6Host
import com.swift.newvpn.utils.wrapIPV6Host
import kotlinx.parcelize.Parcelize


@Parcelize
data class SocksBean(
    var tx: Long = 0L,//上行速度
    var rx: Long = 0L,//下行速度
    var serverAddress: String = "",
    var serverPort: Int = 1080,
    var name: String = "",
    var customOutboundJson: String = "",
    var customConfigJson: String = "",
    @Transient
    var finalAddress: String? = null,
    @Transient
    var finalPort: Int = 0,
    var protocol: Int = 2,
    var code: String = "",
    var username: String = "",
    var password: String = ""
) : Parcelable {

    fun protocolVersionName() = "5"

    fun displayName(): String = name.takeIf { it.isNotEmpty() } ?: displayAddress()

    fun displayAddress(): String {
        return serverAddress.wrapIPV6Host() + ":" + serverPort
    }

    fun initializeDefaultValues() {
        if (serverAddress.isBlank()) {
            serverAddress = "127.0.0.1"
        } else if (serverAddress.startsWith("[") && serverAddress.endsWith("]")) {
            serverAddress = serverAddress.unwrapIPV6Host()
        }
        finalAddress = serverAddress
        finalPort = serverPort
    }

    fun update(bean: SocksBean) {
        tx = bean.tx
        rx = bean.rx
        serverAddress = bean.serverAddress
        serverPort = bean.serverPort
        name = bean.name
        customOutboundJson = bean.customOutboundJson
        customConfigJson = bean.customConfigJson
        finalAddress = bean.finalAddress
        finalPort = bean.finalPort
        protocol = bean.protocol
        code = bean.code
        username = bean.username
        password = bean.password
    }
}




