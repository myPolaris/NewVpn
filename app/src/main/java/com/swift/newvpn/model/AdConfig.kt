package com.swift.newvpn.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AdConfig(
    @Expose(serialize = false)
    var adPos: String,
    @SerializedName("type")
    val adType: String,
    @SerializedName("id")
    val adId: String,
    val enable: Boolean
)

enum class AdPosition(val key: String) {
    InsBack("ad_back"),
    Open("ad_splash"),
    InsConnecting("ad_conn"),
    NativeMain("nav_main"),
    NativeResult("nav_result")
}

data class RemoteAdConfig(
    @SerializedName("ad_conf")
    val adConfigs: Map<String, AdConfig>
)
