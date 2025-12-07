package com.swift.newvpn.utils

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun CoroutineScope.runIO(block: suspend CoroutineScope.() -> Unit) =
    launch(Dispatchers.IO, block = block)

fun CoroutineScope.runMain(block: suspend CoroutineScope.() -> Unit) =
    launch(Dispatchers.Main, block = block)

fun CoroutineScope.runCalculate(block: suspend CoroutineScope.() -> Unit) =
    launch(Dispatchers.Default, block = block)

suspend fun <T> toIO(block: suspend CoroutineScope.() -> T) =
    withContext(Dispatchers.IO, block)

suspend fun <T> toMain(block: suspend CoroutineScope.() -> T) =
    withContext(Dispatchers.Main, block)

suspend fun <T> toCalculate(block: suspend CoroutineScope.() -> T) =
    withContext(Dispatchers.Default, block)

fun CoroutineScope.postDelay(timeMillis: Long, block: suspend () -> Unit) =
    launch(Dispatchers.Main) {
        delay(timeMillis)
        block()
    }
val appScope = GlobalScope

fun noExceptionLauncher(
    block: suspend CoroutineScope.() -> Unit
): Job {
    val handler = CoroutineExceptionHandler { _, _ ->
    }
    return appScope.launch(handler, block = block)
}
