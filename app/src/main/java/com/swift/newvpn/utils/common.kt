package com.swift.newvpn.utils

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri


enum class ActionCheck {
    Equals, Greater, GreaterOrEqual, Less, LessOrEqual
}

private fun Int.checkAndroidVersion(action: ActionCheck = ActionCheck.GreaterOrEqual) =
    when (action) {
        ActionCheck.Equals -> Build.VERSION.SDK_INT == this

        ActionCheck.Greater -> Build.VERSION.SDK_INT > this

        ActionCheck.GreaterOrEqual -> Build.VERSION.SDK_INT >= this

        ActionCheck.Less -> Build.VERSION.SDK_INT < this

        ActionCheck.LessOrEqual -> Build.VERSION.SDK_INT <= this
    }


fun isAndroid8(action: ActionCheck = ActionCheck.GreaterOrEqual) =
    Build.VERSION_CODES.O.checkAndroidVersion(action)

fun isAndroid9(action: ActionCheck = ActionCheck.GreaterOrEqual) =
    Build.VERSION_CODES.P.checkAndroidVersion(action)

fun isAndroid10(action: ActionCheck = ActionCheck.GreaterOrEqual) =
    Build.VERSION_CODES.Q.checkAndroidVersion(action)

fun isAndroid12(action: ActionCheck = ActionCheck.GreaterOrEqual) =
    Build.VERSION_CODES.S.checkAndroidVersion(action)

fun isAndroid13(action: ActionCheck = ActionCheck.GreaterOrEqual) =
    Build.VERSION_CODES.TIRAMISU.checkAndroidVersion(action)

fun isAndroid14(action: ActionCheck = ActionCheck.GreaterOrEqual) =
    Build.VERSION_CODES.UPSIDE_DOWN_CAKE.checkAndroidVersion(action)

fun isAndroid15(action: ActionCheck = ActionCheck.GreaterOrEqual) =
    Build.VERSION_CODES.VANILLA_ICE_CREAM.checkAndroidVersion(action)

fun Activity.checkNotificationPermission() {
    // sdk 33 notification
    if (isAndroid13()) {
        val checkPermission = ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS)
        if (checkPermission != PackageManager.PERMISSION_GRANTED) {
            //动态申请
            ActivityCompat.requestPermissions(this, arrayOf(POST_NOTIFICATIONS), 0)
        }
    }
}

fun Context.notificationPermissionEnable() = !isAndroid13() || ContextCompat.checkSelfPermission(
    this, POST_NOTIFICATIONS
) == PackageManager.PERMISSION_GRANTED


val languageList = mutableListOf(
    "en" to "English",
    "pt" to "Português",
    "zh" to "中文繁體",
    "ko" to "한국인",
    "ja" to "日本語",
    "es" to "Español",
    "de" to "Deutsch",
    "ru" to "Русский язык",
    "fr" to "Français",
)

fun String.toLocale() = when (this) {
    "pt" -> LocalLanguageSet.getPortugalLocale()
    "zh" -> LocalLanguageSet.getChineseLocale()
    "ko" -> LocalLanguageSet.getKoreanLocale()
    "ja" -> LocalLanguageSet.getJapaneseLocale()
    "es" -> LocalLanguageSet.getSpainLocale()
    "de" -> LocalLanguageSet.getGermanLocale()
    "ru" -> LocalLanguageSet.getRussiaLocale()
    "fr" -> LocalLanguageSet.getFrenchLocale()
    else -> LocalLanguageSet.getEnglishLocale()
}

fun Context.openGooglePlayAppDetails() {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = "http://play.google.com/store/apps/details?id=${packageName}".toUri()
        setPackage("com.android.vending")
    }
    startActivity(intent)
}

