package com.swift.newvpn.utils

import com.swift.newvpn.model.KvProxy
import com.tencent.mmkv.MMKV

fun MMKV.long(
    key: String,
    defaultValue: Long = 0L,
) = KvProxy(key, defaultValue, ::decodeLong, ::encode)

fun MMKV.string(
    key: String,
    defaultValue: String = ""
) = KvProxy(key, defaultValue, ::decodeString, ::encode)

fun MMKV.int(
    key: String,
    defaultValue: Int = 0,
) = KvProxy(key, defaultValue, ::decodeInt, ::encode)

fun MMKV.float(
    key: String,
    defaultValue: Float = 0f,
) = KvProxy(key, defaultValue, ::decodeFloat, ::encode)

fun MMKV.bool(
    key: String,
    defaultValue: Boolean = false,
) = KvProxy(key, defaultValue, ::decodeBool, ::encode)

