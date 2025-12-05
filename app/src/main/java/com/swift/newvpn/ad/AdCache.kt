package com.swift.newvpn.ad

import android.util.Log
import com.swift.newvpn.utils.noExceptionLauncher
import com.swift.newvpn.utils.toMain
import com.swift.newvpn.model.AdConfig
import java.util.concurrent.atomic.AtomicReference

class AdCache<T : AdWrapper>(val adPos: String) {
    private val adCache: AtomicReference<T?> = AtomicReference()
    private val tag = "AdsLoader"
    private var isLoading = false
    private var callback: ((Boolean) -> Unit)? = null

    @Synchronized
    private fun addCache(ad: T?) {
        ad?.let {
            synchronized(adCache) {
                adCache.set(it)
            }
        }
    }

    @Synchronized
    fun getAdWrapper(): T? =
        synchronized(adCache) {
            adCache.getAndSet(null)?.takeIf { it.isValid() }
        }

    @Synchronized
    private fun isAdCached(): Boolean =
        synchronized(adCache) {
            adCache.get()?.isValid()?.also {
                if (!it) {
                    adCache.set(null)
                }
            } == true
        }

    fun removeCallback() {
        callback = null
    }

    fun loadAd(
        loadCallback: ((Boolean) -> Unit)? = null,
        block: suspend (AdConfig) -> T?
    ) {
        when {
            !AdUtils.isEnable(adPos) -> {
                loadCallback?.invoke(false)
                return
            }

            isLoading -> {
                loadCallback?.also {
                    callback = it
                }
                return
            }

            isAdCached() -> {
                loadCallback?.invoke(true)
                return
            }
        }

        AdUtils.getAdConfig(adPos)?.also { config ->
            isLoading = true
            callback = loadCallback
            Log.e(tag, "loadAd1: $config")
            noExceptionLauncher {
                val wrapper = block(config)
                addCache(wrapper)
                Log.e(tag, "loadAd2: $wrapper")
                isLoading = false
                toMain {
                    callback?.invoke(wrapper != null)
                    callback = null
                }
            }
        } ?: loadCallback?.invoke(false)
    }
}