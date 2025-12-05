package com.swift.newvpn.utils

import android.app.ActivityManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.PowerManager
import android.os.UserManager
import android.telephony.TelephonyManager
import androidx.core.content.getSystemService

val telephonyManager by lazy { app.getSystemService<TelephonyManager>() }

val activityManager by lazy { app.getSystemService<ActivityManager>() }

val powerManager by lazy { app.getSystemService<PowerManager>() }

val packageManager by lazy { app.packageManager }

val connectivityManager by lazy { app.getSystemService<ConnectivityManager>() }

val notificationManager by lazy { app.getSystemService<NotificationManager>() }

val userManager by lazy { app.getSystemService<UserManager>() }

val keyguardManager by lazy { app.getSystemService<KeyguardManager>() }

val wifiManager by lazy { app.getSystemService<WifiManager>() }
