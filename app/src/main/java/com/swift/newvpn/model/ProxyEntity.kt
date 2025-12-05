package com.swift.newvpn.model

import android.util.Log
import com.google.gson.annotations.SerializedName

data class ProxyEntity(
    @SerializedName("conf_serv")
    val serviceConfig: RemoteServiceConfig
){
    fun toSocksBenList(): List<*> = serviceConfig.lists.map {
        //TODO
        Log.d("ProxyEntity", "toSocksBenList: $it")
    }
}

data class RemoteServiceConfig(
    val lists: List<ProxyServer>
)

data class ProxyServer(
    val country: String,
    val alias: String,
    val server: String,
    val port: Int,
    val user: String,
    val pass: String,
    val ping: Int
)
