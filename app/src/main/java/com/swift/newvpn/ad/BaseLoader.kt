package com.swift.newvpn.ad

import android.os.Bundle
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.OnPaidEventListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.swift.newvpn.utils.app
import com.swift.newvpn.utils.toMain
import com.swift.newvpn.model.AdConfig
import kotlinx.coroutines.CompletableDeferred
import java.math.BigDecimal
import java.util.Currency

abstract class BaseLoader<T : AdWrapper>(val adPos: String) : OnPaidEventListener {
    private val adCache = AdCache<T>(adPos)
    protected val TAG = this.javaClass.simpleName

    override fun onPaidEvent(p0: AdValue) {
        val adValueMicros = p0.valueMicros / 1000_000.toDouble()
        if (FacebookSdk.isInitialized()) {
            AppEventsLogger.newLogger(app)
                .logPurchase(
                    BigDecimal.valueOf(adValueMicros),
                    Currency.getInstance(p0.currencyCode)
                )
        }
        Bundle().apply {
            putDouble(FirebaseAnalytics.Param.VALUE, adValueMicros)
            putString(FirebaseAnalytics.Param.CURRENCY, "USD")
            putString("precisionType", p0.precisionType.toString())
            FirebaseAnalytics.getInstance(app).logEvent("Ad_Impression_Revenue", this)
        }
    }

    protected fun getAdRequest() = AdRequest.Builder().build()

    fun load(loadCallback: ((Boolean) -> Unit)?) {
        loadAd(loadCallback)
    }

    fun preload() {
        loadAd()
    }

    fun removeCallback(){
        adCache.removeCallback()
    }

    private fun loadAd(loadCallback: ((Boolean) -> Unit)? = null) {
        adCache.loadAd(loadCallback) { config ->
            val deferred = CompletableDeferred<T?>()
            toMain {
               getWrapper(config,deferred)
            }
            deferred.await()
        }
    }

    protected fun getAdWrapper() = adCache.getAdWrapper()

   protected abstract fun getWrapper(config: AdConfig, deferred: CompletableDeferred<T?>)

}