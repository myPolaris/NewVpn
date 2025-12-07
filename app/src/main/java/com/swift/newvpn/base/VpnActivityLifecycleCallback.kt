package com.swift.newvpn.base

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.google.android.gms.ads.AdActivity
import com.swift.newvpn.ui.open.OpenActivity

class VpnActivityLifecycleCallback : Application.ActivityLifecycleCallbacks {
    companion object {
        var isPass = false
    }

    private var activityCount = 0

    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?
    ) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle
    ) {
    }

    override fun onActivityStarted(activity: Activity) {
        activity.filter()?.takeIf {
            it.isNeedOpenPage()
        }?.run {
            OpenActivity.start(this)
        }
        isPass = false
    }

    override fun onActivityStopped(activity: Activity) {
        activity.filter()?.let {
            activityCount--
        }
        if (activity is AdActivity) {
            activity.finish()
        }
    }

    private fun Activity.isNeedOpenPage() = activityCount++ == 0 && this !is OpenActivity && !isPass

    private fun Activity.filter() = takeIf { it is BaseActivity<*> }
}