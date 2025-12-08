package com.swift.newvpn.model

import com.google.gson.annotations.SerializedName

data class ProxyEntity(
    @SerializedName("conf_serv")
    val serviceConfig: RemoteServiceConfig
) {
    fun toSocksBenList(): List<SocksBean> = serviceConfig.lists.map {
        it.toSocksBean()
    }
}

data class RemoteServiceConfig(
    val lists: List<ProxyServer>
)

data class ProxyServer(
    val country: String,//国家码,显示国旗
    val alias: String,//显示服务器名称
    val server: String,//host
    val port: Int,//port
    val user: String,//user
    val pass: String,//password
    val ping: Int//ping, 不用显示, 做混淆区分用
) {
    fun toSocksBean() = SocksBean().apply {
        code = country
        protocol = 2
        username = user
        password = pass
        name = alias
        serverAddress = server
        serverPort = port
        initializeDefaultValues()
    }
}