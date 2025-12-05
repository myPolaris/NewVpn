package com.swift.newvpn.utils

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.format.Formatter
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.swift.newvpn.R
import com.swift.newvpn.VpnApp
import com.swift.newvpn.base.BaseActivity
import com.swift.newvpn.base.VpnActivityLifecycleCallback
import com.swift.newvpn.model.SocksBean
import kotlinx.coroutines.flow.Flow
import java.io.File

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

fun SocksBean.getNationalFlagImage() = when (code) {
    "US" -> R.mipmap.usa
    "KR" -> R.mipmap.south_korea
    "JP" -> R.mipmap.japan
    "DE" -> R.mipmap.germany
    "AU" -> R.mipmap.australia
    else -> R.mipmap.usa
}

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

fun @receiver:StringRes Int.getString() = ContextCompat.getString(app, this)

fun @receiver:StringRes Int.getString(vararg formatArgs: Any) = app.getString(this, *formatArgs)

fun Context.getColorAttr(@AttrRes resId: Int): Int {
    return ContextCompat.getColor(this, TypedValue().also {
        theme.resolveAttribute(resId, it, true)
    }.resourceId)
}

fun broadcastReceiver(callback: (Context, Intent) -> Unit): BroadcastReceiver =
    object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = callback(context, intent)
    }

fun Context.startServiceCompat(clazz: Class<*>, block: ((Intent) -> Unit)? = null) {
    Intent(this, clazz).also {
        block?.invoke(it)
        ContextCompat.startForegroundService(this, it)
    }
}

fun <T> MutableLiveData<T>.observeCompat(owner: LifecycleOwner, observer: Observer<T>) {
    observe(owner, observer)
    owner.lifecycle.let {
        it.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                removeObserver(observer)
                it.removeObserver(this)
            }
        })
    }
}

fun <T> Flow<T>.observeCompat(activity: BaseActivity<*>, block: suspend (T) -> Unit) {
    activity.lifecycleScope.runIO {
        collect { item ->
            toMain {
                block(item)
            }
        }
    }
}

val Throwable.readableMessage
    get() = localizedMessage.takeIf { !it.isNullOrBlank() } ?: javaClass.simpleName

fun Context.formatFileSize(size: Long) = R.string.s_speed.getString(
    Formatter.formatFileSize(this, size)
)

fun VpnApp.cleanWebview() {
    var pathToClean = "app_webview"
    if (isBackProcess) pathToClean += "_$process"
    runCatchingDef {
        val dataDir = filesDir.parentFile!!
        File(dataDir, "$pathToClean/BrowserMetrics").recreate(true)
        File(dataDir, "$pathToClean/BrowserMetrics-spare.pma").recreate(false)
    }
}

fun File.recreate(dir: Boolean) {
    if (parentFile?.isDirectory != true) return
    if (dir && !isFile) {
        if (exists()) deleteRecursively()
        createNewFile()
    } else if (!dir && !isDirectory) {
        if (exists()) delete()
        mkdir()
    }
}
