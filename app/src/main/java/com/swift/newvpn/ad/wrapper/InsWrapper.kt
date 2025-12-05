package com.swift.newvpn.ad.wrapper

import com.google.android.gms.ads.interstitial.InterstitialAd
import com.swift.newvpn.ad.AdWrapper

class InsWrapper(private val insAd: InterstitialAd?) : AdWrapper() {
    fun showAd(block: (InterstitialAd) -> Unit): InterstitialAd? {
        setUsed()
        return insAd?.apply(block)
    }
}