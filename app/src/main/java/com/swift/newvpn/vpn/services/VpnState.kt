package com.swift.newvpn.vpn.services

enum class VpnState(
    val canStop: Boolean = false,
    val started: Boolean = false,
    val connected: Boolean = false,
) {
    Idle(),
    Connecting(true, true, false),
    Connected(true, true, true),
    Stopping(),
    Stopped(),
}