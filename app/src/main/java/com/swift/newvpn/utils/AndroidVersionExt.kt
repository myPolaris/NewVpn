package com.swift.newvpn.utils

import android.os.Build


enum class ActionCheck {
    Equals, Greater, GreaterOrEqual, Less, LessOrEqual
}

private fun Int.checkAndroidVersion(action: ActionCheck = ActionCheck.GreaterOrEqual) =
    when (action) {
        ActionCheck.Equals -> Build.VERSION.SDK_INT == this

        ActionCheck.Greater -> Build.VERSION.SDK_INT > this

        ActionCheck.GreaterOrEqual -> Build.VERSION.SDK_INT >= this

        ActionCheck.Less -> Build.VERSION.SDK_INT < this

        ActionCheck.LessOrEqual -> Build.VERSION.SDK_INT <= this
    }

fun isAndroid8(action: ActionCheck = ActionCheck.GreaterOrEqual) =
    Build.VERSION_CODES.O.checkAndroidVersion(action)

fun isAndroid9(action: ActionCheck = ActionCheck.GreaterOrEqual) =
    Build.VERSION_CODES.P.checkAndroidVersion(action)

fun isAndroid10(action: ActionCheck = ActionCheck.GreaterOrEqual) =
    Build.VERSION_CODES.Q.checkAndroidVersion(action)

fun isAndroid11(action: ActionCheck = ActionCheck.GreaterOrEqual) =
    Build.VERSION_CODES.R.checkAndroidVersion(action)

fun isAndroid13(action: ActionCheck = ActionCheck.GreaterOrEqual) =
    Build.VERSION_CODES.TIRAMISU.checkAndroidVersion(action)

fun isAndroid14(action: ActionCheck = ActionCheck.GreaterOrEqual) =
    Build.VERSION_CODES.UPSIDE_DOWN_CAKE.checkAndroidVersion(action)