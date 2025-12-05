package com.swift.newvpn.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.util.Base64
import android.webkit.WebView
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

    fun commandLine2String(args: Iterable<String>?): String {
        // empty path return empty string
        args ?: return ""
        // path containing one or more elements
        return StringBuilder().run {
            for (arg in args) {
                if (isNotEmpty()) append(' ')
                arg.indices.map { arg[it] }.forEach {
                    when (it) {
                        ' ', '\\', '"', '\'' -> {
                            append('\\')  // intentionally no break
                            append(it)
                        }

                        else -> append(it)
                    }
                }
            }
            toString()
        }
    }

    fun handleWebviewDir(context: Context) {
        if (isAndroid9()) {
            return
        }
        runCatching {
            val pathSet: MutableSet<String> = HashSet()
            var suffix: String
            val dataPath = context.dataDir.absolutePath
            val webViewDir = "/app_webview"
            val huaweiWebViewDir = "/app_hws_webview"
            val lockFile = "/webview_data.lock"
            val processName = AppUtils.processName()
            if (BuildConfig.APPLICATION_ID != processName) { //判断不等于默认进程名称
                suffix =
                    if (TextUtils.isEmpty(processName)) context.getPackageName() else processName
                WebView.setDataDirectorySuffix(suffix)
                suffix = "_$suffix"
                pathSet.add(dataPath + webViewDir + suffix + lockFile)
                if (DeviceUtils.isHuaweiRom()) {
                    pathSet.add(dataPath + huaweiWebViewDir + suffix + lockFile)
                }
            } else {
                //主进程
                suffix = "_$processName"
                pathSet.add(dataPath + webViewDir + lockFile) //默认未添加进程名后缀
                pathSet.add(dataPath + webViewDir + suffix + lockFile) //系统自动添加了进程名后缀
                if (DeviceUtils.isHuaweiRom()) { //部分华为手机更改了webview目录名
                    pathSet.add(dataPath + huaweiWebViewDir + lockFile)
                    pathSet.add(dataPath + huaweiWebViewDir + suffix + lockFile)
                }
            }
            for (path in pathSet) {
                val file = File(path)
                if (file.exists()) {
                    Utils.tryLockOrRecreateFile(file)
                    break
                }
            }
        }.onFailure {
            Logs.e(it)
        }
    }
}