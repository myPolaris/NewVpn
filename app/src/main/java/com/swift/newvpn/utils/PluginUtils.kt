package com.swift.newvpn.utils

import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import androidx.core.text.isDigitsOnly
import java.io.File
import java.io.IOException
import kotlin.collections.first
import kotlin.io.bufferedReader
import kotlin.io.inputStream
import kotlin.io.readText
import kotlin.io.use
import kotlin.text.endsWith
import kotlin.text.split
import kotlin.text.toInt

object PluginUtils {
    private val EXECUTABLES = setOf(
        "libtrojan.so", "libtrojan-go.so", "libnaive.so", "libtuic.so", "libhysteria.so"
    )

    fun killAll(alsoKillBg: Boolean = false) {
        // kill bg may fail
        for (process in File("/proc").listFiles { _, name -> name.isDigitsOnly() } ?: return) {
            val exe = File(
                try {
                    File(process, "cmdline").inputStream().bufferedReader().use {
                        it.readText()
                    }
                } catch (_: IOException) {
                    continue
                }.split(Character.MIN_VALUE, limit = 2).first())
            if (EXECUTABLES.contains(exe.name) || (alsoKillBg && exe.name.endsWith(":scapeVpn"))) try {
                Os.kill(process.name.toInt(), OsConstants.SIGKILL)
                Logs.w("SIGKILL ${exe.name} (${process.name}) succeed")
            } catch (e: ErrnoException) {
                if (e.errno != OsConstants.ESRCH) {
                    Logs.w("SIGKILL ${exe.absolutePath} (${process.name}) failed")
                    Logs.w(e)
                }
            }
        }
    }
}