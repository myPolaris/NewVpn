package com.swift.newvpn.utils

import android.provider.Settings
import android.telephony.TelephonyManager

object DeviceUtils {

    fun hasSim() = telephonyManager?.let {
        it.getSimState() > TelephonyManager.SIM_STATE_ABSENT
    } == true

    fun isDeveloper() =
        getSecureSettingInt(Settings.Global.ADB_ENABLED) != 0 ||
        getSecureSettingInt(Settings.Global.DEVELOPMENT_SETTINGS_ENABLED) != 0

    fun getSecureSettingInt(name: String, def: Int = 0) = Settings.Secure.getInt(appContentResolver, name, def)

    fun isDeviceInMainlandChina() = (telephonyManager?.simCountryIso?.lowercase() == "cn"
            || telephonyManager?.networkCountryIso?.lowercase() == "cn"
            || isChinaLocale())

    fun isChinaLocale(): Boolean = java.util.Locale.getDefault().let {
        it.country.equals("CN", ignoreCase = true) ||
                it.language.equals("zh", ignoreCase = true)
    }
}