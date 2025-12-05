package com.swift.newvpn.ad.wrapper

import com.google.android.gms.ads.nativead.NativeAd
import com.swift.newvpn.ad.AdWrapper

class NativeWrapper(private var nativeAd: NativeAd?) : AdWrapper() {

    fun showAd(block: (NativeAd) -> Unit): NativeAd? {
        setUsed()
        return nativeAd?.apply(block)
    }

    fun onDestroy() {
        nativeAd?.destroy()
        nativeAd = null
    }
}