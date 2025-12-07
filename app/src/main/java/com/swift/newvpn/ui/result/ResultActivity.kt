package com.swift.newvpn.ui.result

import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import com.swift.newvpn.R
import com.swift.newvpn.ad.AdProxy
import com.swift.newvpn.base.BaseActivity
import com.swift.newvpn.base.ExtraKey
import com.swift.newvpn.databinding.PageResultBinding
import com.swift.newvpn.utils.setNativeDef

class ResultActivity : BaseActivity<PageResultBinding>() {
    companion object {
        fun start(context: Context, isStop: Boolean) {
            Intent(context, ResultActivity::class.java).apply {
                putExtra(ExtraKey.CONNECT_ACTION, isStop)
                context.startActivity(this)
            }
        }
    }

    override fun getViewBinding() = PageResultBinding.inflate(layoutInflater)

    override fun initUI(bind: PageResultBinding) {
        bind.toolbar.root.initToolbar()
        bind.navGroup.setNativeDef()
        changeUI(bind)
    }

    private fun changeUI(bind: PageResultBinding) {
       val isStop = intent.getBooleanExtra(ExtraKey.CONNECT_ACTION,false)
        bind.run {
            if (isStop) {
                imgConnectionSate.setImageResource(R.mipmap.disconnected)
                tvConnectionState.text =
                    getString(R.string.s_disconnected_successful)
            } else {
                imgConnectionSate.setImageResource(R.mipmap.successful)
                tvConnectionState.text = getString(R.string.s_connection_successful)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        withBind {
            loadAd(it.navGroup)
        }
    }

    private fun loadAd(group: ViewGroup) {
        AdProxy.adResult.showAd(this, group)
    }

    override fun onDestroy() {
        AdProxy.adResult.onDestroy()
        super.onDestroy()
    }
}