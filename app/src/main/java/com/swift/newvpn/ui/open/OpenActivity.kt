package com.swift.newvpn.ui.open

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.VpnService
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.swift.newvpn.base.BaseActivity
import com.swift.newvpn.base.ExtraKey
import com.swift.newvpn.base.NextPageEvent
import com.swift.newvpn.base.ShowAdEvent
import com.swift.newvpn.databinding.PageOpenBinding
import com.swift.newvpn.ui.dialog.DeviceInChainDialog
import com.swift.newvpn.ui.home.MainActivity
import com.swift.newvpn.utils.DeviceUtils
import com.swift.newvpn.utils.postDelay
import com.swift.newvpn.utils.isNotificationPermissionEnable
import com.swift.newvpn.utils.observeCompat

class OpenActivity : BaseActivity<PageOpenBinding>() {

    override fun getViewBinding(): PageOpenBinding = PageOpenBinding.inflate(layoutInflater)

    private val viewModel: OpenViewModel by viewModels()

    private var reloadLaunch = false

    override fun initUI(bind: PageOpenBinding) {
        reloadLaunch = intent.getBooleanExtra(ExtraKey.HOT_LAUNCHER, false)
        setPermissionLauncher { prepareVpn() }
        setActivityResultLauncher {}
        initViewModel(bind)
        checkPermission()
    }

    private fun initViewModel(bind: PageOpenBinding) {
        viewModel.let {
            it.maxProgress.observeCompat(this) { max ->
                bind.progressIndicator.max = max
            }
            it.progress.observeCompat(this) { progress ->
                bind.progressIndicator.progress = progress
            }
            it.event.observeCompat(this) { event ->
                when (event) {
                    is NextPageEvent -> goNextPage()
                    is ShowAdEvent -> it.showAd(this)
                }
            }
            it.start()
        }
    }

    private fun checkPermission() {
        lifecycleScope.postDelay(800) {
            when {
                DeviceUtils.isDeviceInMainlandChina() -> {
                    viewModel.stop()
                    showDeviceInChainDialog()
                }

                isNotificationPermissionEnable() -> {
                    prepareVpn()
                }

                else -> {
                    requestPermission(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private lateinit var deviceInChainDialog: DeviceInChainDialog

    fun showDeviceInChainDialog() {
        if (!::deviceInChainDialog.isInitialized) {
            deviceInChainDialog = DeviceInChainDialog(this)
        }
        deviceInChainDialog.show()
    }

    private fun prepareVpn() {
        VpnService.prepare(this)?.launchForResult()
    }

    private fun goNextPage() {
        if (!reloadLaunch) {
            MainActivity.start(this)
        }
        finish()
    }

    override fun onDestroy() {
        if (::deviceInChainDialog.isInitialized) {
            deviceInChainDialog.dismiss()
        }
        super.onDestroy()
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