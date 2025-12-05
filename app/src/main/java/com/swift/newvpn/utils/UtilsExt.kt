package com.swift.newvpn.utils

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.swift.newvpn.R
import com.swift.newvpn.VpnApp
import com.swift.newvpn.base.VpnActivityLifecycleCallback

val app = VpnApp.vpnApp
val packageName = app.packageName
val appContentResolver = app.contentResolver

inline fun <R> runCatchingDef(block: () -> R) = runCatching {
    block()
}.onFailure {
    it.printStackTrace()
}

inline fun <T, R> T.runCatchingDef(block: (T) -> R) = runCatching {
    block(this)
}.onFailure {
    it.printStackTrace()
}.getOrNull()


fun Context.isNotificationPermissionEnable() = !isAndroid13() || ContextCompat.checkSelfPermission(
    this, POST_NOTIFICATIONS
) == PackageManager.PERMISSION_GRANTED

fun Context.openGooglePlayAppDetails() {
    Intent(Intent.ACTION_VIEW).apply {
        data = "http://play.google.com/store/apps/details?id=${packageName}".toUri()
        setPackage("com.android.vending")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        VpnActivityLifecycleCallback.isPass = true
        startActivity(this)
    }
}

fun Context.openOnWeb(@StringRes resId: Int) {
    Intent(Intent.ACTION_VIEW).apply {
        data = getString(resId).toUri()
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        resolveActivity(packageManager)?.let { _ ->
            VpnActivityLifecycleCallback.isPass = true
            startActivity(this)
        }
    }
}

//fun SocksBean.getNationalFlagImage() = when (code) {
//    "US" -> R.mipmap.flag_usa
//    "KR" -> R.mipmap.flag_south_korea
//    "JP" -> R.mipmap.flag_japan
//    "DE" -> R.mipmap.flag_germany
//    "AU" -> R.mipmap.flag_australia
//    else -> R.mipmap.flag_usa
//}

fun Context.getRewardHintStr() = getString(R.string.s_open_ad_add_time).let {
    SpannableString(it).apply {
        val startIndex = 43
        val endIndex = 45
        setSpan(
            ForegroundColorSpan(Color.RED),
            startIndex,
            endIndex,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        setSpan(
            UnderlineSpan(),
            startIndex,
            endIndex,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
    }
}