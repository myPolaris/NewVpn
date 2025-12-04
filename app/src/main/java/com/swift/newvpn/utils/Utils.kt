package com.swift.newvpn.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Base64
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.StringRes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import com.swift.newvpn.BuildConfig
import java.io.File
import java.io.RandomAccessFile

object Utils {

    // gson
    val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .setNumberToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        .setLenient()
        .disableHtmlEscaping()
        .create()

//    fun startService() = ContextCompat.startForegroundService(
//        app, Intent(app, NekoVpnConnection.serviceClass)
//    )
//
//    fun reloadService() =
//        app.sendBroadcast(Intent(Action.RELOAD).setPackage(packageName))
//
//    fun stopService() =
//        app.sendBroadcast(Intent(Action.CLOSE).setPackage(packageName))

    private fun showToast(context: Context, res: String, flag: Int) {
        Toast.makeText(context, res, flag).show()
    }

    fun showToast(context: Context, res: String) {
        showToast(context, res, Toast.LENGTH_LONG)
    }

    fun showShortToast(context: Context, res: String) {
        showToast(context, res, Toast.LENGTH_SHORT)
    }

    fun showToast(context: Context, @StringRes resId: Int) {
        showToast(context, context.getString(resId))
    }

    fun showShortToast(context: Context, @StringRes resId: Int) {
        showShortToast(context, context.getString(resId))
    }

//    fun configureIntent(context: Context): PendingIntent = PendingIntent.getActivity(
//        context,
//        0,
//        Intent(
//            app, MainActivity::class.java
//        ).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
//        PendingIntent.FLAG_IMMUTABLE
//    )

    fun tryLockOrRecreateFile(file: File) {
        if (isAndroid9()) {
            runCatching {
                RandomAccessFile(file, "rw").channel.tryLock()?.also {
                    it.close()
                } ?: createFile(file, file.delete())
            }.onFailure {
                it.printStackTrace()
                val deleted = file.takeIf { f -> f.exists() }?.delete() ?: false
                createFile(file, deleted)
            }
        }
    }

    private fun createFile(file: File, deleted: Boolean) {
        runCatching {
            file.takeIf { deleted && !it.exists() }?.createNewFile()
        }.onFailure { it.printStackTrace() }
    }

    fun b64EncodeUrlSafe(s: String): String {
        return b64EncodeUrlSafe(s.toByteArray())
    }

    fun b64EncodeUrlSafe(b: ByteArray): String {
        return String(Base64.encode(b, Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE))
    }

    fun b64DecodeUrlSafe(s: String): String {
        return b64DecodeUrlSafe(s.toByteArray())
    }

    fun b64DecodeUrlSafe(b: ByteArray): String {
        return String(Base64.decode(b, Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE))
    }

    fun b64Decode(b: String): ByteArray {
        var ret: ByteArray? = null

        // padding 自动处理，不用理
        // URLSafe 需要替换这两个，不要用 URL_SAFE 否则处理非 Safe 的时候会乱码
        val str = b.replace("-", "+").replace("_", "/")

        val flags = listOf(
            Base64.DEFAULT, // 多行
            Base64.NO_WRAP, // 单行
        )

        for (flag in flags) {
            runCatching {
                ret = Base64.decode(str, flag)
            }
            if (ret != null) return ret
        }

        throw IllegalStateException("Cannot decode base64")
    }

    fun map2StringMap(m: Map<*, *>): MutableMap<String, Any?> {
        val o = mutableMapOf<String, Any?>()
        m.forEach {
            if (it.key is String) {
                o[it.key as String] = it.value as Any
            }
        }
        return o
    }

    fun mergeMap(dst: MutableMap<String, Any?>, src: Map<String, Any?>): MutableMap<String, Any?> {
        src.forEach { (k, v) ->
            if (v is Map<*, *> && dst[k] is Map<*, *>) {
                val currentMap = (dst[k] as Map<*, *>).toMutableMap()
                dst[k] = mergeMap(map2StringMap(currentMap), map2StringMap(v))
            } else if (v is List<*>) {
                if (k.startsWith("+")) {  // prepend
                    val dstKey = k.removePrefix("+")
                    var currentList = (dst[dstKey] as? List<*>)?.toMutableList() ?: mutableListOf()
                    currentList = (v + currentList).toMutableList()
                    dst[dstKey] = currentList
                } else if (k.endsWith("+")) {  // append
                    val dstKey = k.removeSuffix("+")
                    var currentList = (dst[dstKey] as? List<*>)?.toMutableList() ?: mutableListOf()
                    currentList = (currentList + v).toMutableList()
                    dst[dstKey] = currentList
                } else {
                    dst[k] = v
                }
            } else {
                dst[k] = v
            }
        }
        return dst
    }

    fun mergeJSON(dst: MutableMap<String, Any?>, j: String?) {
        if (j.isNullOrBlank()) return
        val src = gson.fromJson(j, dst.javaClass)
        mergeMap(dst, src)
    }

    @SuppressLint("WrongConstant")
    fun collapseStatusBar(context: Context) {
        runCatching {
            context.getSystemService("statusbar")
                .apply {
                    javaClass.getMethod("collapsePanels")
                        .invoke(this)
                }
        }
    }


    @get:SuppressLint("PrivateApi")
    val processName: String
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                return Application.getProcessName()
            return runCatching {
                val activityThread =
                    Class.forName("android.app.ActivityThread")
                val methodName = "currentProcessName"
                val getProcessName =
                    activityThread.getDeclaredMethod(methodName)
                getProcessName.invoke(null) as String
            }.getOrDefault(BuildConfig.APPLICATION_ID)
        }

    fun transparentStatusBar(window: Window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        var systemUiVisibility = window.decorView.systemUiVisibility
        systemUiVisibility =
            systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.decorView.systemUiVisibility = systemUiVisibility
        window.statusBarColor = Color.TRANSPARENT

        //设置状态栏文字颜色
        setStatusBarTextColor(window, true)
    }

    fun setStatusBarTextColor(window: Window, light: Boolean) {
        var systemUiVisibility = window.decorView.systemUiVisibility
        systemUiVisibility = if (light) { //白色文字
            systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        } else { //黑色文字
            systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        window.decorView.systemUiVisibility = systemUiVisibility
    }
}