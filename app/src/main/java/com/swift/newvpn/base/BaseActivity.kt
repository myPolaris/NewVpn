package com.swift.newvpn.base

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.TypedValue
import android.util.TypedValue.applyDimension
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.swift.newvpn.ad.AdProxy
import com.swift.newvpn.ui.dialog.LoadingDialog
import com.swift.newvpn.ui.home.MainActivity

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    protected val TAG = javaClass.simpleName
    protected lateinit var binding: VB
    private var isHomePage = false

    //***************** ui *******************

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBackPressedDispatcher()
        binding = getViewBinding()
        setScreenDensity()
        setContentView(binding.root)
        if (isFitSystemWindows()) {
            fitsSystemWindows(binding.root)
        }
        initUI(binding)
    }

    abstract fun getViewBinding(): VB
    abstract fun initUI(bind: VB)

    fun withBind(block: (VB) -> Unit) {
        if (::binding.isInitialized) {
            block(binding)
        }
    }

    private fun setScreenDensity() {
        resources.displayMetrics.apply {
            val height = heightPixels / 760f
            density = height
            applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, this)
            densityDpi = (160 * height).toInt()
        }
    }

    fun Toolbar.initToolbar(isHomePage: Boolean = false) {
        this@BaseActivity.isHomePage = isHomePage
        setSupportActionBar(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(!isHomePage)
        supportActionBar?.title = " "
        setNavigationOnClickListener {
            onBackPressImpl()
        }
    }

    fun fitsSystemWindows(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    open fun isFitSystemWindows() = true

    //******************* lifecycle *******************

    private var isPause = false

    fun isActivityPaused() = isPause

    override fun onResume() {
        super.onResume()
        isPause = false
    }

    override fun onPause() {
        super.onPause()
        isPause = true
    }

    override fun onStop() {
        super.onStop()
        isPause = true
    }

    //***************** backPress *******************

    fun setBackPressedDispatcher() {
        onBackPressedDispatcher.addCallback(this) {
            onBackPressImpl()
        }
    }

    open fun onBackPressImpl() {
        if (isHomePage) {
            finish()
        } else {
            showBackAd()
        }
    }

    open fun showBackAd() {
        AdProxy.adBack.showAd(this) {
            MainActivity.start(this)
        }.takeIf { !it }?.let {
            MainActivity.start(this)
        }
    }

    //******************* permission *******************
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    fun setPermissionLauncher(callback: ((Boolean) -> Unit)?) {
        if (::permissionLauncher.isInitialized)
            return
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                callback?.invoke(isGranted)
            }
    }

    fun requestPermission(permission: String) {
        if (::permissionLauncher.isInitialized) {
            permissionLauncher.launch(permission)
        }
    }

    fun checkAndRequestPermission(permission: String) {
        permission.takeIf {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }?.apply {
            requestPermission(this)
        }
    }
    //********************** Activity result *******************

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    fun setActivityResultLauncher(callback: (ActivityResult) -> Unit) {
        if (::activityResultLauncher.isInitialized)
            return
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                callback.invoke(result)
            }
    }

    fun Intent.launchForResult() {
        if (::activityResultLauncher.isInitialized) {
            activityResultLauncher.launch(this)
        }
    }

    //************************** loading dialog **************************

    private lateinit var loadingDialog: LoadingDialog

    fun showLoadingDialog() {
        if (!::loadingDialog.isInitialized) {
            loadingDialog = LoadingDialog(this@BaseActivity)
        }
        loadingDialog.show()
    }

    fun hideLoadingDialog() {
        if (::loadingDialog.isInitialized) {
            loadingDialog.dismiss()
        }
    }

    override fun onDestroy() {
        hideLoadingDialog()
        super.onDestroy()
    }
}