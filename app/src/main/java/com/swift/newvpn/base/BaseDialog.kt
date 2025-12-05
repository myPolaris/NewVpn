package com.swift.newvpn.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.swift.newvpn.R
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

abstract class BaseDialog<VB : ViewBinding>(context: Context) :
    Dialog(context, R.style.BaseDialogTheme) {
    protected val scope = MainScope()
    private lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindView(layoutInflater)
        withBind {
            setContentView(it.root)
            initUI(it)
        }
    }

    protected fun withBind(block: (VB) -> Unit) {
        if (::binding.isInitialized) {
            block(binding)
        }
    }

    protected abstract fun initUI(bind: VB)

    protected abstract fun bindView(inflater: LayoutInflater): VB

    override fun dismiss() {
        scope.cancel()
        super.dismiss()
    }
}