package com.swift.newvpn.ui.proxy

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.swift.newvpn.base.KvCache
import com.swift.newvpn.databinding.ItemProxyBinding
import com.swift.newvpn.model.SocksBean
import com.swift.newvpn.utils.ProxyManager
import com.swift.newvpn.utils.getNationalFlagImage

class ListAdapter : BaseQuickAdapter<SocksBean, ListAdapter.VH>() {
    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): VH = VH(parent)
    private var lastIndex = -1

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        item: SocksBean?
    ) {
        item?.let { proxy ->
            holder.binding.apply {
                proxy.let {
                    tvProxyName.text = it.name
                    imgNationalFlag.setImageResource(it.getNationalFlagImage())
                }
                proxy.changeCheck(this, position)
            }
        }
    }

    fun onItemClick(p: Int) = getItem(p).takeIf {
        it.name != KvCache.selectedProxy
    }?.also { item ->
        ProxyManager.setSelectedProxy(item.name)
        if (lastIndex > -1) {
            notifyItemChanged(lastIndex)
        }
        notifyItemChanged(p)
    }

    fun SocksBean.changeCheck(binding: ItemProxyBinding, p: Int) {
        val isSelected = name == KvCache.selectedProxy
        binding.cardBg.isSelected = isSelected
        if (isSelected) {
            lastIndex = p
        }
    }

    class VH(
        parent: ViewGroup,
        val binding: ItemProxyBinding = ItemProxyBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ),
    ) : RecyclerView.ViewHolder(binding.root)
}