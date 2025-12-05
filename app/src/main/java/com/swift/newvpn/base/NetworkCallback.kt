package com.swift.newvpn.base
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.swift.newvpn.utils.Logs
import com.swift.newvpn.utils.appScope
import com.swift.newvpn.utils.connectivityManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.runBlocking
import java.net.UnknownHostException

object NetworkCallback {
    private sealed class NetworkMessage {
        class Start(val key: Any, val listener: (Network?) -> Unit) : NetworkMessage()
        class Get : NetworkMessage() {
            val response = CompletableDeferred<Network>()
        }

        class Stop(val key: Any) : NetworkMessage()

        class Put(val network: Network) : NetworkMessage()
        class Update(val network: Network) : NetworkMessage()
        class Lost(val network: Network) : NetworkMessage()
    }

    private val networkActor = appScope.actor<NetworkMessage>(Dispatchers.Unconfined) {
        val listeners = mutableMapOf<Any, (Network?) -> Unit>()
        var network: Network? = null
        val pendingRequests = arrayListOf<NetworkMessage.Get>()
        for (message in channel) when (message) {
            is NetworkMessage.Start -> {
                if (listeners.isEmpty()) register()
                listeners[message.key] = message.listener
                if (network != null) message.listener(network)
            }

            is NetworkMessage.Get -> {
                check(listeners.isNotEmpty()) { "Getting network without any listeners is not supported" }
                if (network == null) pendingRequests += message else message.response.complete(
                    network
                )
            }

            is NetworkMessage.Stop -> if (listeners.isNotEmpty() && // was not empty
                listeners.remove(message.key) != null && listeners.isEmpty()
            ) {
                network = null
                unregister()
            }

            is NetworkMessage.Put -> {
                network = message.network
                pendingRequests.forEach { it.response.complete(message.network) }
                pendingRequests.clear()
                listeners.values.forEach { it(network) }
            }

            is NetworkMessage.Update -> if (network == message.network) listeners.values.forEach {
                it(
                    network
                )
            }

            is NetworkMessage.Lost -> if (network == message.network) {
                network = null
                listeners.values.forEach { it(null) }
            }
        }
    }

    suspend fun start(key: Any, listener: (Network?) -> Unit) =
        networkActor.send(NetworkMessage.Start(key, listener))

    suspend fun get() = if (fallback)  {
        connectivityManager?.activeNetwork
            ?: throw UnknownHostException() // failed to listen, return current if available
    } else NetworkMessage.Get().run {
        networkActor.send(this)
        response.await()
    }

    suspend fun stop(key: Any) = networkActor.send(NetworkMessage.Stop(key))

    // NB: this runs in ConnectivityThread, and this behavior cannot be changed until API 26
    private object Callback : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) =
            runBlocking { networkActor.send(NetworkMessage.Put(network)) }

        override fun onCapabilitiesChanged(
            network: Network, networkCapabilities: NetworkCapabilities
        ) { // it's a good idea to refresh capabilities
            runBlocking { networkActor.send(NetworkMessage.Update(network)) }
        }

        override fun onLost(network: Network) =
            runBlocking { networkActor.send(NetworkMessage.Lost(network)) }
    }
    private var fallback = false
    private val request = NetworkRequest.Builder().apply {
        addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
    }.build()

    private val mainHandler = Handler(Looper.getMainLooper())

    private fun register() {
        runCatching {
            fallback = false
            connectivityManager?.run {
                when (Build.VERSION.SDK_INT) {
                    in Build.VERSION_CODES.S..Int.MAX_VALUE -> {
                        registerBestMatchingNetworkCallback(
                            request, Callback, mainHandler
                        )
                    }

                    in Build.VERSION_CODES.P until Build.VERSION_CODES.S -> {  // we want REQUEST here instead of LISTEN
                       requestNetwork(request, Callback, mainHandler)
                    }

                    in Build.VERSION_CODES.O until Build.VERSION_CODES.P -> {
                       registerDefaultNetworkCallback(Callback, mainHandler)
                    }

                    in Build.VERSION_CODES.N until Build.VERSION_CODES.O -> {
                       registerDefaultNetworkCallback(Callback)
                    }

                    else -> {
                       requestNetwork(request, Callback)
                        // known bug on API 23: https://stackoverflow.com/a/33509180/2245107
                    }
                }
            }
        }.onFailure {
            Logs.w(it)
            fallback = true
        }
    }

    private fun unregister() = connectivityManager?.unregisterNetworkCallback(Callback)
}