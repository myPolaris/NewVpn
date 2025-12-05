package com.swift.newvpn.ad.loader

import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.swift.newvpn.model.AdConfig
import com.swift.newvpn.model.AdPosition
import com.swift.newvpn.ad.AdUtils
import com.swift.newvpn.ad.BaseLoader
import com.swift.newvpn.ad.wrapper.OpenWrapper
import com.swift.newvpn.base.BaseActivity
import com.swift.newvpn.utils.app
import kotlinx.coroutines.CompletableDeferred

class OpenLoader : BaseLoader<OpenWrapper>(AdPosition.Open.key) {
    override fun getWrapper(
        config: AdConfig,
        deferred: CompletableDeferred<OpenWrapper?>
    ) {
        AppOpenAd.load(
            app,
            config.adId,
            getAdRequest(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(openAd: AppOpenAd) {
                    super.onAdLoaded(openAd)
                    openAd.onPaidEventListener = this@OpenLoader
                    Log.d(TAG, "loadOpen: load ad success pos: $adPos")
                    deferred.complete(OpenWrapper(openAd))
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    Log.d(TAG, "loadOpen: load ad fail pos: $adPos error: ${p0.message}")
                    deferred.complete(null)
                }
            })
    }

    fun showAd(
        activity: BaseActivity<*>,
        callback: (() -> Unit)? = null
    ): Boolean {
        if (activity.isActivityPaused() || !AdUtils.isEnable(adPos)) return false
        return getAdWrapper()?.showAd {
            it.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    preload()
                    callback?.invoke()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    preload()
                    Log.d(TAG, "showOpen: show ad fail error: ${p0.message}")
                    callback?.invoke()
                }
            }
            it.show(activity)
        } != null
    }
}