@file:Suppress("SpellCheckingInspection")

package com.swift.newvpn.utils

fun String.isIpAddress(): Boolean {
    return isIpv4Address(this) || isIpv6Address(this)
}

fun String.isIpAddressV6(): Boolean {
    return isIpv6Address(this)
}

fun String.unwrapIPV6Host(): String {
    if (startsWith("[") && endsWith("]")) {
        return substring(1, length - 1).unwrapIPV6Host()
    }
    return this
}

fun String.wrapIPV6Host(): String {
    val unwrapped = this.unwrapIPV6Host()
    if (unwrapped.isIpAddressV6()) {
        return "[$unwrapped]"
    } else {
        return this
    }
}

fun isIpv4Address(value: String): Boolean {
    val regV4 =
        Regex("^([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])$")
    return regV4.matches(value)
}

fun isIpv6Address(value: String): Boolean {
    var addr = value
    if (addr.indexOf("[") == 0 && addr.lastIndexOf("]") > 0) {
        addr = addr.drop(1)
        addr = addr.dropLast(addr.count() - addr.lastIndexOf("]"))
    }
    val regV6 =
        Regex("^([0-9A-Fa-f]{1,4})?(:[0-9A-Fa-f]{1,4})*::([0-9A-Fa-f]{1,4})?(:[0-9A-Fa-f]{1,4})*|([0-9A-Fa-f]{1,4})(:[0-9A-Fa-f]{1,4}){7}$")
    return regV6.matches(addr)
}

