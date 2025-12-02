package com.swift.newvpn

import android.app.Application
import androidx.work.Configuration as WorkContinuation

class VApp : Application(), WorkContinuation.Provider {
    override val workManagerConfiguration: WorkContinuation
        get() = WorkContinuation.Builder().setDefaultProcessName("${BuildConfig.APPLICATION_ID}:newVpn").build()
}