package com.swift.newvpn.utils

import android.util.Log
import libcore.Libcore
import java.io.InputStream
import java.io.OutputStream
import kotlin.io.copyTo
import kotlin.io.use
import kotlin.stackTraceToString
import kotlin.text.substringAfterLast

object Logs {

    private fun mkTag(): String {
        val stackTrace = Thread.currentThread().stackTrace
        return stackTrace[4].className.substringAfterLast(".")
    }

    fun d(message: String) {
        Libcore.nekoLogPrintln("[Debug] [${mkTag()}] $message")
        Log.d("${mkTag()}", message)
    }

    fun d(message: String, exception: Throwable) {
        Log.d("${mkTag()}", message)
        Libcore.nekoLogPrintln("[Debug] [${mkTag()}] $message" + "\n" + exception.stackTraceToString())
    }

    fun i(message: String) {
        Log.i("${mkTag()}", message)
        Libcore.nekoLogPrintln("[Info] [${mkTag()}] $message")
    }

    fun w(message: String) {
        Log.w("${mkTag()}", message)
        Libcore.nekoLogPrintln("[Warning] [${mkTag()}] $message")
    }

    fun w(exception: Throwable) {
        Log.w("${mkTag()}", exception.stackTraceToString())
        Libcore.nekoLogPrintln("[Warning] [${mkTag()}] " + exception.stackTraceToString())
    }

    fun e(message: String) {
        Log.e("${mkTag()}", message)
        Libcore.nekoLogPrintln("[Error] [${mkTag()}] $message")
    }

    fun e(message: String, exception: Throwable) {
        Log.e("${mkTag()}", message)
        Libcore.nekoLogPrintln("[Error] [${mkTag()}] $message" + "\n" + exception.stackTraceToString())
    }

    fun e(exception: Throwable) {
        Log.e("${mkTag()}", exception.stackTraceToString())
        Libcore.nekoLogPrintln("[Error] [${mkTag()}] " + exception.stackTraceToString())
    }

}