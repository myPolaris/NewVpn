package com.swift.newvpn.ad

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.swift.newvpn.databinding.LayoutNativeAdBinding

class NavAdViewGroup : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet?, defId: Int) : super(
        context,
        attributeSet,
        defId
    ) {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    private var mNativeAd: NativeAd? = null
    private fun obtainLayoutInflater(): LayoutInflater {
        return context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mNativeAd?.destroy()
        mNativeAd = null
    }

    fun show(nativeAd: NativeAd) {
        mNativeAd?.destroy()
        mNativeAd = nativeAd
        val adView = initView().bindNative(nativeAd)
        visibility = VISIBLE
        removeAllViews()
        addView(adView)
    }

    private fun initView() =
        LayoutNativeAdBinding.inflate(obtainLayoutInflater()).apply {
            tvAdFlag.visibility = VISIBLE
            root.isEnabled = true
            title.isEnabled = true
            body.isEnabled = true
            action.isEnabled = true

            root.mediaView = mediaView
            root.headlineView = title
            root.bodyView = body
            root.callToActionView = action
            root.iconView = imgIcon


            root.bodyView?.visibility = INVISIBLE
            root.callToActionView?.visibility = INVISIBLE
            root.iconView?.visibility = GONE
        }.root

    private fun NativeAdView.bindNative(nativeAd: NativeAd) = also { root ->
        (root.headlineView as? TextView)?.text = nativeAd.headline
        nativeAd.body?.let {
            root.bodyView?.run {
                visibility = VISIBLE
                (this as? TextView)?.text = it
            }
        }
        nativeAd.callToAction?.let {
            root.callToActionView?.run {
                visibility = VISIBLE
                (this as? TextView)?.text = it
            }
        }
        nativeAd.icon?.let {
            root.iconView?.run {
                visibility = VISIBLE
                (this as? ImageView)?.setImageDrawable(it.drawable)
            }
        }
        nativeAd.mediaContent?.let {
            root.mediaView?.mediaContent = it
        }
        root.setNativeAd(nativeAd)
    }
}