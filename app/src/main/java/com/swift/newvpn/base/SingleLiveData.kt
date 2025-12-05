package com.swift.newvpn.base

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

class SingleLiveData<T>(v: T) : MutableLiveData<T>(v) {
    private val isUsed = AtomicBoolean(false)

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner) { t ->
            if (isUsed.compareAndSet(false, true)) {
                observer.onChanged(t)
            }
        }
    }

    override fun setValue(t: T?) {
        isUsed.set(false)
        super.setValue(t)
    }
}