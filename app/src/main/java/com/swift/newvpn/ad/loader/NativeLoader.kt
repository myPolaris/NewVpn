package com.swift.newvpn.ad.loader

import android.util.Log
import android.view.ViewGroup
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.swift.newvpn.model.AdConfig
import com.swift.newvpn.ad.AdUtils
import com.swift.newvpn.ad.BaseLoader
import com.swift.newvpn.ad.NavAdViewGroup
import com.swift.newvpn.ad.wrapper.NativeWrapper
import com.swift.newvpn.base.BaseActivity
import com.swift.newvpn.utils.app
import kotlinx.coroutines.CompletableDeferred

class NativeLoader(adPos: String) : BaseLoader<NativeWrapper>(adPos) {
    private var wrapper: NativeWrapper? = null
    override fun getWrapper(
        config: AdConfig,
        deferred: CompletableDeferred<NativeWrapper?>
    ) {
        AdLoader.Builder(app, config.adId)
            .forNativeAd { nativeAd ->
                nativeAd.setOnPaidEventListener(this)
                deferred.complete(NativeWrapper(nativeAd))
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    Log.e(TAG, "loadNative: load ad fail pos: ${config.adPos} error: ${p0.message}")
                    deferred.complete(null)
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build()
            )
            .build()
            .loadAd(getAdRequest())
    }

    fun showAd(
        activity: BaseActivity<*>,
        adGroup: ViewGroup?
    ): Boolean {
        if (activity.isActivityPaused() || adGroup == null || !AdUtils.isEnable(adPos)) return false
        return getAdWrapper().apply {
            wrapper = this
        }?.showAd {
            NavAdViewGroup(adGroup.context).apply {
                show(it)
                adGroup.removeAllViews()
                adGroup.addView(this)
            }
            preload()
        } != null
    }

    fun onDestroy() {
        wrapper?.onDestroy()
    }
}