package com.swift.newvpn

import android.app.Application
import android.content.Context
import androidx.work.Configuration as WorkContinuation

class VApp : Application(), WorkContinuation.Provider {
    companion object {
        lateinit var app: VApp
    }

    override val workManagerConfiguration: WorkContinuation
        get() = WorkContinuation.Builder()
            .setDefaultProcessName("${BuildConfig.APPLICATION_ID}:newVpn").build()

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        app = this
    }

    override fun onCreate() {
        super.onCreate()

    }
}