package com.swift.newvpn.ad.wrapper

import com.google.android.gms.ads.appopen.AppOpenAd
import com.swift.newvpn.ad.AdWrapper

class OpenWrapper(private val openAd: AppOpenAd?) : AdWrapper() {
    fun showAd(block: (AppOpenAd) -> Unit): AppOpenAd? {
        setUsed()
        return openAd?.apply(block)
    }
}