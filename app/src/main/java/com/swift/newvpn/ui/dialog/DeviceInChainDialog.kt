package com.swift.newvpn.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import com.swift.newvpn.base.BaseDialog
import com.swift.newvpn.databinding.LayoutDeviceChainDialogBinding
import com.swift.newvpn.utils.AppUtils

class DeviceInChainDialog(context: Context) : BaseDialog<LayoutDeviceChainDialogBinding>(context) {

    override fun bindView(inflater: LayoutInflater) =
        LayoutDeviceChainDialogBinding.inflate(inflater)

    override fun initUI(bind: LayoutDeviceChainDialogBinding) {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        bind.btnOk.setOnClickListener {
            AppUtils.exitApp()
            dismiss()
        }
    }
}