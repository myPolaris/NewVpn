package com.swift.newvpn.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import com.swift.newvpn.base.BaseDialog
import com.swift.newvpn.databinding.LayoutLoadingDialogBinding

class LoadingDialog(context: Context) : BaseDialog<LayoutLoadingDialogBinding>(context) {
    override fun initUI(bind: LayoutLoadingDialogBinding) {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    override fun bindView(inflater: LayoutInflater) = LayoutLoadingDialogBinding.inflate(inflater)
}