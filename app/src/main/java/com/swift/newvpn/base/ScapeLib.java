package com.swift.newvpn.base;

import android.content.Context;

import androidx.annotation.Keep;

@Keep
public class ScapeLib {
    static {
        System.loadLibrary("scape");
    }

    private ScapeLib() {
        // 私有构造，防止实例化
    }

    @Keep
    public static native String core(Context context);

    @Keep
    public static native void init(Context context);
}
