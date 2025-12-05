package com.swift.newvpn.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SpeedData(
    // Bytes per second
    var txRateProxy: Long = 0L,
    var rxRateProxy: Long = 0L,
    var txRateDirect: Long = 0L,
    var rxRateDirect: Long = 0L,
    var txTotal: Long = 0L,
    var rxTotal: Long = 0L,
) : Parcelable

@Parcelize
data class TrafficData(
    var name: String = "",
    var tx: Long = 0L,
    var rx: Long = 0L,
) : Parcelable

