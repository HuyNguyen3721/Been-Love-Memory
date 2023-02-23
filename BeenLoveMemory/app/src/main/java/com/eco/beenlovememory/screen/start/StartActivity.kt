package com.eco.beenlovememory.screen.start

import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import com.eco.beenlovememory.screen.main.MainActivity
import com.eco.beenlovememory.ads.InterstitialAdUtil
import com.eco.beenlovememory.base.BaseActivity
import com.eco.beenlovememory.databinding.ActivitySplashBinding
import com.eco.beenlovememory.extension.launchActivity
import com.eco.beenlovememory.utils.IAPUtils

class StartActivity : BaseActivity<ActivitySplashBinding>(), InterstitialAdUtil.AdsListener {
    private var isFirstOpen = false
    override fun initData() {
    }

    override fun initView() {
        loadAds()
        setAppActivityFullScreenOverStatusBar(this)
    }


    override fun initListener() {
    }

    override fun viewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(LayoutInflater.from(this))
    }

    override fun beforeOnCreate() {
    }

    override fun observable() {
    }

    override fun onInternetConnected() {
    }

    override fun onAdClosed() {
        skip()
    }

    override fun onAdFullClicked() {
    }

    override fun onAdsFullShow() {
    }

    override fun onAdsShowFail() {
    }

    private fun loadAds() {

    }

    private fun startTimeLoadAds() {
        jobLoadAds.setAdsShowed(false)
        jobLoadAds.startJob {
            if (!isActive()) {
                return@startJob
            }
            if (jobLoadAds.isShowAds()) {
                return@startJob
            }
            jobLoadAds.setDelay(80)
            if (IAPUtils.getInstance().isPurchased() || !permissionUtils.isNetworkAvailable(this)
            ) {
                if (it >= 30) skip()
            } else {
                if (it < 99) {
                    if (it > 64) {
                        binding.prLoading.isVisible = true
                    }
                    if (interstitialAdUtil?.adsFullLoaded() == true && !isLeftScreen) {
                        interstitialAdUtil?.showFullAds()
                        binding.prLoading.visibility = View.VISIBLE
                        jobLoadAds.setAdsShowed(true)
                        jobLoadAds.stopJob()
                    }
                } else if (interstitialAdUtil?.isLoadFailedAds() == true) {
                    skip()
                } else {
                    skip()
                }
            }
        }
    }

    private fun skip() {
        jobLoadAds.setAdsShowed(false)
        jobLoadAds.stopJob()
        launchActivity<MainActivity> {
        }
        finish()
    }

    override fun onResume() {
        super.onResume()
        isLeftScreen = false
    }

    override fun onStop() {
        super.onStop()
        isLeftScreen = true
    }

}
