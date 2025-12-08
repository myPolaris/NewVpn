package com.swift.newvpn.utils

import android.app.KeyguardManager
import android.app.NotificationManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.PowerManager
import android.telephony.TelephonyManager
import androidx.core.content.getSystemService

val telephonyManager by lazy { app.getSystemService<TelephonyManager>() }
val powerManager by lazy { app.getSystemService<PowerManager>() }
val connectivityManager by lazy { app.getSystemService<ConnectivityManager>() }
val notificationManager by lazy { app.getSystemService<NotificationManager>() }
val keyguardManager by lazy { app.getSystemService<KeyguardManager>() }
val wifiManager by lazy { app.getSystemService<WifiManager>() }