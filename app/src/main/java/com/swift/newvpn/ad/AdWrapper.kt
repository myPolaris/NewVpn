package com.swift.newvpn.ad

abstract class AdWrapper {
    private var isUsed = false
    fun isValid() = !isUsed
    fun setUsed() {
        isUsed = true
    }
}