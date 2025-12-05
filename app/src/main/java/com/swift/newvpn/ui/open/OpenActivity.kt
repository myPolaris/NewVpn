package com.swift.newvpn.ui.open

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.swift.newvpn.ad.AdProxy
import com.swift.newvpn.base.BaseActivity
import com.swift.newvpn.base.ExtraKey
import com.swift.newvpn.databinding.PageOpenBinding
import com.swift.newvpn.ui.home.MainActivity
import com.swift.newvpn.utils.delayed
import com.swift.newvpn.utils.isNotificationPermissionEnable

class OpenActivity: BaseActivity<PageOpenBinding>() {

    override fun getViewBinding(): PageOpenBinding = PageOpenBinding.inflate(layoutInflater)

    override fun initUI(bind: PageOpenBinding) {
        AdProxy.adBack.preload()
        AdProxy.adConnect.preload()
        setPermissionLauncher {  }

        lifecycleScope.delayed(2000){
            MainActivity.start(this)
        }

        if (!isNotificationPermissionEnable()){
            requestPermission(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun isFitSystemWindows() = false

    companion object {
        fun start(activity: Activity) {
            Intent(activity, OpenActivity::class.java).apply {
                putExtra(ExtraKey.HOT_LAUNCHER, true)
                activity.startActivity(this)
            }
        }
    }
}