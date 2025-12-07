package com.swift.newvpn.ui.setting

import android.app.Activity
import android.content.Intent
import com.swift.newvpn.R
import com.swift.newvpn.base.BaseActivity
import com.swift.newvpn.databinding.PageSettingBinding
import com.swift.newvpn.utils.openGooglePlayAppDetails
import com.swift.newvpn.utils.openOnWeb

class SettingActivity : BaseActivity<PageSettingBinding>() {

    companion object {
        fun start(activity: Activity) {
            Intent(activity, SettingActivity::class.java).apply {
                activity.startActivity(this)
            }
        }
    }

    override fun getViewBinding() = PageSettingBinding.inflate(layoutInflater)

    override fun initUI(bind: PageSettingBinding) {
        bind.toolbar.root.initToolbar()
        bind.cardCheckUp.setOnClickListener {
            openGooglePlayAppDetails()
        }
        bind.cardShare.setOnClickListener {
            openGooglePlayAppDetails()
        }
        bind.cardPrivacyPolicy.setOnClickListener{
            openOnWeb(R.string.s_privacy_policy_url)
        }
    }
}