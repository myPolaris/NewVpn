package com.swift.newvpn.ui.proxy

import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.swift.newvpn.base.BaseActivity
import com.swift.newvpn.databinding.PageProxyListBinding
import com.swift.newvpn.utils.observeCompat
import com.swift.newvpn.utils.postDelay

class ProxyListActivity : BaseActivity<PageProxyListBinding>() {
    private var isChanged = false

    val viewModel: ProxyListViewModel by viewModels()

    private var listAdapter: ListAdapter? = null
    override fun getViewBinding() = PageProxyListBinding.inflate(layoutInflater)

    override fun initUI(bind: PageProxyListBinding) {
        bind.toolbar.root.initToolbar()
        bind.rvProxyList.run {
            adapter = ListAdapter().apply {
                listAdapter = this
                setOnItemClickListener { _, _, position ->
                   onItemClick(position)?.let {
                       isChanged = true
                       lifecycleScope.postDelay(300){
                           finish()
                       }
                   }
                }
            }
        }
        viewModel.proxyList.observeCompat(this) {
            listAdapter?.submitList(it)
        }

    }

    override fun finish() {
        setResult(if (isChanged) RESULT_OK else RESULT_CANCELED)
        super.finish()
    }

    override fun onDestroy() {
        listAdapter?.setOnItemClickListener(null)
        super.onDestroy()
    }
}