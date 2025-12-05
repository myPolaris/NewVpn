package com.swift.newvpn.vpn

import android.net.DnsResolver
import android.os.Build
import android.os.CancellationSignal
import android.system.ErrnoException
import androidx.annotation.RequiresApi
import com.swift.newvpn.base.KvCache
import com.swift.newvpn.utils.Logs
import com.swift.newvpn.utils.appScope
import com.swift.newvpn.utils.isAndroid10
import com.swift.newvpn.utils.runIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import libcore.ExchangeContext
import libcore.LocalDNSTransport
import java.net.InetAddress
import java.net.UnknownHostException

object LocalResolverImpl : LocalDNSTransport {

    private const val RCODE_NXDOMAIN = 3

    override fun raw(): Boolean {
        return isAndroid10()
    }

    override fun networkHandle(): Long {
        return if (isAndroid10()) {
            KvCache.underlyingNetwork?.networkHandle ?: 0
        } else 0
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun exchange(ctx: ExchangeContext, message: ByteArray) {
        val signal = CancellationSignal()
        ctx.onCancel(signal::cancel)

        val callback = object : DnsResolver.Callback<ByteArray> {
            override fun onAnswer(answer: ByteArray, rcode: Int) {
                ctx.rawSuccess(answer)
            }

            override fun onError(error: DnsResolver.DnsException) {
                val cause = error.cause
                if (cause is ErrnoException) {
                    ctx.errnoCode(cause.errno)
                } else {
                    Logs.w(error)
                    ctx.errnoCode(114514)
                }
            }
        }

        DnsResolver.getInstance().rawQuery(
            KvCache.underlyingNetwork,
            message,
            DnsResolver.FLAG_NO_RETRY,
            Dispatchers.IO.asExecutor(),
            signal,
            callback
        )
    }

    override fun lookup(ctx: ExchangeContext, network: String, domain: String) {
        if (isAndroid10()) {
            val signal = CancellationSignal()
            ctx.onCancel(signal::cancel)

            val callback = object : DnsResolver.Callback<Collection<InetAddress>> {
                override fun onAnswer(answer: Collection<InetAddress>, rcode: Int) {
                    runCatching {
                        if (rcode == 0) {
                            ctx.success(answer.mapNotNull { it.hostAddress }.joinToString("\n"))
                        } else {
                            ctx.errorCode(rcode)
                        }
                    }.onFailure {
                        Logs.w(it)
                        ctx.errnoCode(114514)
                    }
                }

                override fun onError(error: DnsResolver.DnsException) {
                    runCatching {
                        val cause = error.cause
                        if (cause is ErrnoException) {
                            ctx.errnoCode(cause.errno)
                        } else {
                            Logs.w(error)
                            ctx.errnoCode(114514)
                        }
                    }.onFailure {
                        Logs.w(it)
                        ctx.errnoCode(114514)
                    }
                }
            }

            val type = when {
                network.endsWith("4") -> DnsResolver.TYPE_A
                network.endsWith("6") -> DnsResolver.TYPE_AAAA
                else -> null
            }
            type?.apply {
                DnsResolver.getInstance().query(
                    KvCache.underlyingNetwork,
                    domain,
                    type,
                    DnsResolver.FLAG_NO_RETRY,
                    Dispatchers.IO.asExecutor(),
                    signal,
                    callback
                )
            } ?: DnsResolver.getInstance().query(
                KvCache.underlyingNetwork,
                domain,
                DnsResolver.FLAG_NO_RETRY,
                Dispatchers.IO.asExecutor(),
                signal,
                callback
            )
        } else {
            appScope.runIO {
                // 老版本系统，继续用阻塞的 InetAddress
                runCatching {
                    val u = KvCache.underlyingNetwork
                    val answer = if (u != null) {
                        u.getAllByName(domain)
                    } else {
                        InetAddress.getAllByName(domain)
                    }
                    if (answer != null) {
                        ctx.success(answer.mapNotNull { it.hostAddress }.joinToString("\n"))
                    } else {
                        ctx.errnoCode(114514)
                    }
                }.onFailure {
                    if (it is UnknownHostException){
                        ctx.errorCode(RCODE_NXDOMAIN)
                    } else {
                        Logs.w(it)
                        ctx.errnoCode(114514)
                    }
                }
            }
        }
    }
}