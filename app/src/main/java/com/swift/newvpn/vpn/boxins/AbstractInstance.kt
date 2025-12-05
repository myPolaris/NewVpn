package com.swift.newvpn.vpn.boxins

import java.io.Closeable

interface AbstractInstance : Closeable {
    fun launch()
}