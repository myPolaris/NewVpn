package com.swift.newvpn.ad.loader

import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.swift.newvpn.model.AdConfig
import com.swift.newvpn.ad.AdUtils
import com.swift.newvpn.ad.BaseLoader
import com.swift.newvpn.ad.wrapper.InsWrapper
import com.swift.newvpn.base.BaseActivity
import com.swift.newvpn.utils.app
import kotlinx.coroutines.CompletableDeferred

class InsLoader(adPos: String) : BaseLoader<InsWrapper>(adPos) {
    override fun getWrapper(
        config: AdConfig,
        deferred: CompletableDeferred<InsWrapper?>
    ) {
        InterstitialAd.load(
            app,
            config.adId,
            getAdRequest(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    Log.d(TAG, "loadInterstitial: load ad fail pos: $adPos error: ${p0.message}")
                    deferred.complete(null)
                }

                override fun onAdLoaded(insAd: InterstitialAd) {
                    super.onAdLoaded(insAd)
                    insAd.onPaidEventListener = this@InsLoader
                    Log.d(TAG, "loadInterstitial: load ad success pos: $adPos")
                    deferred.complete(InsWrapper(insAd))
                }
            })
    }

    fun showAd(
        activity: BaseActivity<*>,
        callback: ((Boolean) -> Unit)? = null
    ): Boolean {
        if (activity.isActivityPaused() || !AdUtils.isEnable(adPos)) return false
        return getAdWrapper()?.showAd {
            it.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    preload()
                    callback?.invoke(false)
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    preload()
                    Log.d(TAG, "showOpen: show ad fail pos: $adPos error: ${p0.message}")
                    callback?.invoke(false)
                }
            }
            it.show(activity)
        } != null
    }
}