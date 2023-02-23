package com.eco.beenlovememory.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewbinding.ViewBinding
import com.eco.beenlovememory.R
import com.eco.beenlovememory.ads.InterstitialAdUtil
import com.eco.beenlovememory.broadcast.ChangeInterNetBroadcast
import com.eco.beenlovememory.di.listModule
import com.eco.beenlovememory.screen.start.JobLoadAds
import com.eco.beenlovememory.utils.Constants
import com.eco.beenlovememory.utils.Constants.MY_PERMISSIONS_REQUEST
import com.eco.beenlovememory.utils.IAPUtils
import com.eco.beenlovememory.utils.PermissionUtils
import com.eco.beenlovememory.utils.PreferencesUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.serenegiant.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.fragment.android.setupKoinFragmentFactory
import org.koin.androidx.scope.LifecycleScopeDelegate
import org.koin.androidx.scope.activityScope
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.scope.Scope

abstract class BaseActivity<B : ViewBinding> : AppCompatActivity(), AndroidScopeComponent,
    ChangeInterNetBroadcast.ChangeInternetListener {
    val localBroadcastManager by lazy { LocalBroadcastManager.getInstance(this) }
    var interstitialAdUtil: InterstitialAdUtil? = null
    lateinit var binding: B
    private var lastTimeClicked: Long = 0
    private var permissionComplete: ((Boolean) -> Unit)? = null
    var isLeftScreen = false
    val jobLoadAds: JobLoadAds by lazy { JobLoadAds() }
    val permissionUtils: PermissionUtils by lazy { PermissionUtils() }
    val fileUtils: FileUtils by lazy { FileUtils() }
    var isBannerOrNativeFailLoad = false
    var isClickViewBack = false
    var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null
    override val scope: Scope by contextAwareActivityScope()
    open abstract fun initData()
    open abstract fun initView()
    open abstract fun initListener()
    open abstract fun viewBinding(): B
    open abstract fun beforeOnCreate()
    open abstract fun observable()
    var onDelete: ((Boolean) -> Unit)? = null
    var onRename: ((Boolean) -> Unit)? = null
    private var changeInterNetBroadcast: ChangeInterNetBroadcast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        beforeOnCreate()
        super.onCreate(savedInstanceState)
//        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        // setLanguage(getCurrentLanguageUtils())
        binding = viewBinding()
        setContentView(binding.root)
        setupKoinFragmentFactory(scope)
        initData()
        initView()
        initListener()
        observable()
        checkInternetChange()
    }

    override fun onDestroy() {
        if (changeInterNetBroadcast != null) {
            localBroadcastManager.unregisterReceiver(changeInterNetBroadcast!!)
        }
        super.onDestroy()
    }

    open val launcherDelete: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                onDelete?.invoke(true)
                toast(getString(R.string.delete_success))
            } else {
                onDelete?.invoke(false)
                toast(getString(R.string.delete_fail))
            }
        }
    open val launcherRename: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                onRename?.invoke(true)
                toast(getString(R.string.rename_successed))
            } else {
                onRename?.invoke(false)
                toast(getString(R.string.rename_fail))
            }
        }

    private fun addView(view: View) {
        addView(view)
    }

    fun isDoubleClick(): Boolean {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < 500) {
            return true
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        return false
    }

    fun toast(content: String?) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (!TextUtils.isEmpty(content)) Toast.makeText(
                this@BaseActivity,
                content,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    open fun requestPermission(
        complete: (Boolean) -> Unit, requestCode: Int = MY_PERMISSIONS_REQUEST,
        vararg permissions: String?
    ) {
        this.permissionComplete = complete
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !permissionUtils.checkPermissionAccept(
                this,
                *permissions
            )
        ) {
            permissionUtils.requestRuntimePermission(this, requestCode, *permissions)
        } else {
            complete.invoke(true)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionUtils.REQUEST_CODE -> if (permissionUtils.checkPermissionAccept(
                    this,
                    *permissions
                )
            ) {
                permissionComplete?.invoke(true)
            } else {
                permissionComplete?.invoke(false)
            }
        }
    }

    open fun setAppActivityFullScreen(activity: FragmentActivity) {
        val window: Window = activity.window
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        //make fully Android Transparent Status bar
        setWindowFlag(
            activity,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
            false
        )
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.statusBarColor = Color.TRANSPARENT
    }

    open fun setAppActivityFullScreenOverStatusBar(activity: FragmentActivity) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        val window: Window = activity.window
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        //make fully Android Transparent Status bar
        setWindowFlag(
            activity,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
            true
        )
        window.statusBarColor = Color.TRANSPARENT
    }

    open fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    open fun isActive(): Boolean {
        return !isFinishing && !isDestroyed
    }

    open fun changeStatusBarColor(activity: Activity, color: Int, isTextColorLight: Boolean) {
        val window: Window = activity.window
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            val winParams = window.attributes
            winParams.flags = winParams.flags or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            window.attributes = winParams
            window.statusBarColor = Color.TRANSPARENT
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
                !isTextColorLight
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ResourcesCompat.getColor(activity.resources, color, null)
        }
    }

    open fun setStatusBarHomeTransparent(activity: FragmentActivity) {
        val window: Window = activity.window
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        if (Build.VERSION.SDK_INT in 19..20) {
            setWindowFlag(
                activity,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                true
            )
        }
        //make fully Android Transparent Status bar
        setWindowFlag(
            activity,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
            false
        )
        window.statusBarColor = Color.TRANSPARENT
    }

    open fun animTouch(view: View) {
        val anim = AnimationUtils.loadAnimation(this, R.anim.anim_touch_view)
        view.startAnimation(anim)
    }

    open fun animDisTouch(view: View) {
        val anim = AnimationUtils.loadAnimation(this, R.anim.anim_distouch_view)
        view.startAnimation(anim)
    }

    @SuppressLint("ClickableViewAccessibility")
    open fun animClickScaleView(view: View, endAmin: () -> Unit) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    animTouch(view)
                }
                MotionEvent.ACTION_UP -> {
                    animDisTouch(view)
                    endAmin.invoke()
                }
                MotionEvent.ACTION_MOVE -> {
                }
            }
            true
        }
    }

    fun animRotateCycle(view: View, duration: Long = 3500) {
        view.clearAnimation()
        val rotate = RotateAnimation(
            0F, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotate.duration = duration
        rotate.interpolator = LinearInterpolator()
        rotate.repeatCount = Animation.INFINITE
        view.animation = rotate
    }

    open fun checkInternetChange() {
        changeInterNetBroadcast = ChangeInterNetBroadcast(this, this)
        localBroadcastManager.registerReceiver(
            changeInterNetBroadcast!!,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }

    fun isBoughIAP(): Boolean {
        return IAPUtils.getInstance().isPurchased()
    }


    protected fun loadAdsInter(AdsId: String) {
//        if (!isBoughIAP()) {
//            interstitialAdUtil = InterstitialAdUtil(this)
//            interstitialAdUtil?.loadAdsFullScreenGoogle(AdsId)
//        }
    }

    protected fun isScreenSmall(): Boolean {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        return height <= 1800 || height == 2560
    }

    override fun onBackPressed() {
        if (!isClickViewBack) {
/*            AnalyticsManager.getInstance()
                .postEventBundle(EventManager.System_BackButton_String(localClassName))*/
            isClickViewBack = false
        }
        super.onBackPressed()
    }

    protected open fun getValueFromRemoteConfig() {
        if (!PreferencesUtils.getBoolean(Constants.PREFS_GET_DATA_FROM_REMOTE) && permissionUtils.isNetworkAvailable(
                this
            )
        ) {
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(10)
                .build()
            mFirebaseRemoteConfig?.setConfigSettingsAsync(configSettings)
            mFirebaseRemoteConfig?.setDefaultsAsync(R.xml.remote_config_defaults)
            mFirebaseRemoteConfig!!.fetchAndActivate()
                .addOnCompleteListener(
                    this
                ) { task: Task<Boolean?> ->
                    if (task.isSuccessful && !isDestroyed) {
                        initDataFromRemote(task, mFirebaseRemoteConfig!!)
                        PreferencesUtils.putBoolean(Constants.PREFS_GET_DATA_FROM_REMOTE, true)
                    }
                }
        }
    }

    protected open fun initDataFromRemote(
        task: Task<Boolean?>,
        mFirebaseRemoteConfig: FirebaseRemoteConfig
    ) {
        val updated = task.result
    }

    private fun ComponentActivity.contextAwareActivityScope() = runCatching {
        LifecycleScopeDelegate<Activity>(
            lifecycleOwner = this, koin = getKoin()
        )
    }.getOrElse { activityScope() }

    private fun ComponentActivity.getKoin(): Koin {
        return if (this is KoinComponent) {
            getKoin()
        } else {
            GlobalContext.getOrNull() ?: startKoin {
                androidContext(applicationContext)
                modules(listModule)
            }.koin
        }
    }

    open fun isIAPBoughtSetting(): Boolean {
        return /*prefs.getBoolean(Constants.PREFS_PURCHASED)*/ true
    }


    override fun onResume() {
        super.onResume()
        isLeftScreen = false
    }

    override fun onPause() {
        super.onPause()
        isLeftScreen = true
    }
}
