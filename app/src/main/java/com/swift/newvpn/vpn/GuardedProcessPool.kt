package com.swift.newvpn.vpn

import android.os.SystemClock
import android.system.OsConstants
import androidx.annotation.MainThread
import com.swift.newvpn.utils.Logs
import com.swift.newvpn.utils.Utils
import com.swift.newvpn.utils.app
import com.swift.newvpn.utils.isAndroid8
import com.swift.newvpn.utils.runCatchingDef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import libcore.Libcore
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.concurrent.thread

class GuardedProcessPool(private val onFatal: suspend (IOException) -> Unit) : CoroutineScope {
    companion object {
        private val pid by lazy {
            Class.forName($$"java.lang.ProcessManager$ProcessImpl").getDeclaredField("pid")
                .apply { isAccessible = true }
        }
    }

    private inner class Guard(
        private val cmd: List<String>,
        private val env: Map<String, String> = mapOf()
    ) {
        private lateinit var process: Process

        private fun streamLogger(input: InputStream, logger: (String) -> Unit) =
            runCatchingDef { input.bufferedReader().forEachLine(logger) }

        fun start() {
            process = ProcessBuilder(cmd)
                .directory(app.noBackupFilesDir)
                .apply {
                    environment().putAll(env)
                }.start()
        }

        @DelicateCoroutinesApi
        suspend fun looper(onRestartCallback: (suspend () -> Unit)?) {
            var running = true
            val cmdName = File(cmd.first()).nameWithoutExtension
            val exitChannel = Channel<Int>()
            try {
                while (true) {
                    thread(name = "stderr-$cmdName") {
                        streamLogger(process.errorStream) { Libcore.nekoLogPrintln("[$cmdName] $it") }
                    }
                    thread(name = "stdout-$cmdName") {
                        streamLogger(process.inputStream) { Libcore.nekoLogPrintln("[$cmdName] $it") }
                        // this thread also acts as a daemon thread for waitFor
                        runBlocking { exitChannel.send(process.waitFor()) }
                    }
                    val startTime = SystemClock.elapsedRealtime()
                    val exitCode = exitChannel.receive()
                    running = false
                    when {
                        SystemClock.elapsedRealtime() - startTime < 1000 -> throw IOException(
                            "$cmdName exits too fast (exit code: $exitCode)"
                        )

                        exitCode == 128 + OsConstants.SIGKILL -> Logs.w("$cmdName was killed")
                        else -> Logs.w(IOException("$cmdName unexpectedly exits with code $exitCode"))
                    }
                    Logs.i("restart process: ${Utils.commandLine2String(cmd)} (last exit code: $exitCode)")
                    start()
                    running = true
                    onRestartCallback?.invoke()
                }
            } catch (e: IOException) {
                GlobalScope.launch(Dispatchers.Main) { onFatal(e) }
            } finally {
                if (running) withContext(NonCancellable) {  // clean-up cannot be cancelled
                    process.destroy()                       // kill the process
                    if (isAndroid8()) {
                        if (withTimeoutOrNull(1000) { exitChannel.receive() } != null) return@withContext
                        process.destroyForcibly()           // Force to kill the process if it's still alive
                    }
                    exitChannel.receive()
                }                                           // otherwise process already exited, nothing to be done
            }
        }
    }

    override val coroutineContext = Dispatchers.Main.immediate + Job()
    var processCount = 0

    @MainThread
    fun start(
        cmd: List<String>,
        env: MutableMap<String, String> = mutableMapOf(),
        onRestartCallback: (suspend () -> Unit)? = null
    ) {
        Logs.i("start process: ${Utils.commandLine2String(cmd)}")
        Guard(cmd, env).apply {
            start() // if start fails, IOException will be thrown directly
            launch { looper(onRestartCallback) }
        }
        processCount += 1
    }

    @MainThread
    fun close(scope: CoroutineScope) {
        cancel()
        coroutineContext[Job.Key]?.also { job -> scope.launch { job.cancelAndJoin() } }
    }
}