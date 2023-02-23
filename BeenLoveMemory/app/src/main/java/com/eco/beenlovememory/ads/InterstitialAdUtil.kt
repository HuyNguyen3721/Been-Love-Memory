package com.eco.beenlovememory.ads

import android.app.Activity
import androidx.annotation.NonNull
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterstitialAdUtil(activity: Activity) {
    private var activity: Activity? = activity
    private var mInterstitialAd: InterstitialAd? = null
    private var listener: AdsListener? = null
    private var isAdsShowed = false
    var adStatus = 0
    private val adLoaded = 1
    private val adFailed = 2
    private var isCallLoadFullAds = false

    init {
        listener = activity as AdsListener
    }

    fun isCallLoadFullAds(): Boolean {
        return isCallLoadFullAds
    }

    fun loadAdsFullScreenGoogle(adUnitId: String) {
        adStatus = 0
        activity?.let {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(it, adUnitId, adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(@NonNull interstitialAd: InterstitialAd) {
//                        val adSourceName = interstitialAd.getAdSourceName()
                        interstitialAd.setOnPaidEventListener { adValue ->
//                            AppsFlyer.logAdRevenueAppsFlyer(
//                                adValue,
//                                adUnitId,
//                                adSourceName,
//                                AppsFlyerAdNetworkEventType.INTERSTITIAL
//                            )
                        }
                        mInterstitialAd = interstitialAd
                        adStatus = adLoaded
                        listenerFullScreenContentCallback()
                    }

                    override fun onAdFailedToLoad(@NonNull loadAdError: LoadAdError) {
                        adStatus = adFailed
                        mInterstitialAd = null
                    }
                })
        }
    }

    private fun listenerFullScreenContentCallback() {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                listener?.onAdFullClicked()
            }

            override fun onAdDismissedFullScreenContent() {
                mInterstitialAd = null
                listener?.onAdClosed()

            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                mInterstitialAd = null
                listener?.onAdsShowFail()
            }

            override fun onAdImpression() {
            }

            override fun onAdShowedFullScreenContent() {
                listener?.onAdsFullShow()
            }
        }
    }

    fun isLoadFailedAds(): Boolean {
        return adStatus == adFailed
    }

    fun adsFullLoaded(): Boolean {
        return try {
            mInterstitialAd != null && adStatus == adLoaded
        } catch (e: NullPointerException) {
            false
        }
    }

    fun showFullAds() {
        isAdsShowed = true
//        (activity!!.application as SlideApplication).setAdsFullShowing(true)
        activity?.let {
            mInterstitialAd?.show(it)
        }
    }

    fun adsShowed(): Boolean {
        return isAdsShowed
    }

    interface AdsListener {
        fun onAdClosed()
        fun onAdFullClicked()
        fun onAdsFullShow()
        fun onAdsShowFail()
    }
}