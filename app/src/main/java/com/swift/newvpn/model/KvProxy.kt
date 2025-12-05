package com.swift.newvpn.model

import kotlin.reflect.KProperty

class KvProxy<T>(
    val key: String,
    val defaultValue: T,
    val getter: (String, T) -> T?,
    val setter: (String, value: T) -> Unit,
) {

    operator fun setValue(thisObj: Any?, property: KProperty<*>, value: T) {
        setter(key, value)
    }

    operator fun getValue(thisObj: Any?, property: KProperty<*>) =
        getter(key, defaultValue) ?: defaultValue
}
