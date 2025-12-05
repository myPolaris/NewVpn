package com.swift.newvpn.ad

import com.swift.newvpn.ad.loader.InsLoader
import com.swift.newvpn.ad.loader.NativeLoader
import com.swift.newvpn.ad.loader.OpenLoader
import com.swift.newvpn.model.AdPosition

object AdProxy {
    val adOpen by lazy { OpenLoader() }
    val adConnect by lazy { InsLoader(AdPosition.InsConnecting.key) }
    val adBack by lazy { InsLoader(AdPosition.InsBack.key) }
    val adResult by lazy { NativeLoader(AdPosition.NativeResult.key) }
    val adMain by lazy { NativeLoader(AdPosition.NativeMain.key) }
}