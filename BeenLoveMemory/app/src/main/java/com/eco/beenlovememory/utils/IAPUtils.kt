package com.eco.beenlovememory.utils

import android.app.Activity
import android.app.Application
import com.android.billingclient.api.*
import com.google.firebase.crashlytics.FirebaseCrashlytics

class IAPUtils {
    private val skuDetailsList = ArrayList<SkuDetails>()
    private var application: Application? = null

    companion object {
        const val KEY_PREMIUM_ONE_MONTH = "sub_1month_28.8.22"
        const val KEY_PREMIUM_ONE_YEAR = "sub_1year_3days_trial2"
        const val KEY_PREMIUM_LIFE_TIME = "iap_lifetime_purchase_28.8.22"
        var INSTANCE: IAPUtils? = null
        fun getInstance(): IAPUtils {
            if (INSTANCE == null) {
                INSTANCE = IAPUtils()
            }
            return INSTANCE!!
        }
    }

    private var billingClient: BillingClient? = null
    fun isPurchased(): Boolean {
        return /*PreferencesUtils.getBoolean(Constants.PREFS_PURCHASED, false)*/ false
    }

    fun isPremium(result: (Boolean) -> Unit) {
        isSubscriptions(KEY_PREMIUM_ONE_MONTH) {
            if (it) {
                result.invoke(true)
            } else {
                isSubscriptions(KEY_PREMIUM_ONE_YEAR) {
                    if (it) {
                        result.invoke(true)
                    } else {
                        isPurchased(KEY_PREMIUM_LIFE_TIME) {
                            if (it) {
                                result.invoke(true)
                            } else {
                                result.invoke(false)
                            }
                        }
                    }
                }
            }
        }
    }

    fun isLifeTime(result: (Boolean) -> Unit) {
        isPurchased(KEY_PREMIUM_LIFE_TIME) {
            result.invoke(it)
        }
    }


    // init call in application
    fun init(application: Application, complete: (Boolean) -> Unit) {
        this.application = application
        billingClient = BillingClient.newBuilder(application)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    getInApp()
                    getAllSubscriptions()
                    //clearIapHistory();
                    PreferencesUtils.putBoolean(Constants.PREFS_PURCHASED, isPurchased())
                    complete.invoke(true)
                } else {
                    complete.invoke(false)
                }
            }

            override fun onBillingServiceDisconnected() {
                complete.invoke(false)
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    private fun clearIapHistory(onCallComplete: (Boolean) -> Unit) {
        if (billingClient == null || !billingClient!!.isReady) {
            onCallComplete.invoke(false)
            return
        }
        billingClient!!.queryPurchasesAsync(
            BillingClient.SkuType.INAPP
        ) { p0, purchases ->
            if (purchases.isEmpty()) {
                onCallComplete.invoke(false)
            } else {
                for (purchase in purchases) {
                    val params =
                        ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
                            .build()
                    billingClient!!.consumeAsync(
                        params
                    ) { _, s -> }
                }
            }
            onCallComplete.invoke(true)
        }

    }

    fun historyBill(listenerHistory: (Int) -> Unit) {
        if (billingClient == null || !billingClient!!.isReady) {
            listenerHistory.invoke(0)
            return
        }
        billingClient!!.queryPurchaseHistoryAsync(
            BillingClient.SkuType.INAPP
        ) { _: BillingResult?, list: List<PurchaseHistoryRecord?>? ->
            if (list == null) {
                listenerHistory.invoke(0)
            } else {
                listenerHistory.invoke(list.size)
            }
        }
    }


    // get in app
    fun getInApp() {
        val skuListToQuery: MutableList<String> = java.util.ArrayList()
        skuListToQuery.add(KEY_PREMIUM_LIFE_TIME)
        val skuDetailsParams = SkuDetailsParams.newBuilder()
            .setSkusList(skuListToQuery)
            .setType(BillingClient.SkuType.INAPP)
            .build()
        billingClient!!.querySkuDetailsAsync(
            skuDetailsParams
        ) { _, list ->
            if (list != null && list.isNotEmpty()) {
                skuDetailsList.addAll(list)
            }
        }
    }

    // get all subcription
    fun getAllSubscriptions() {
        val skuListToQuery: MutableList<String> = java.util.ArrayList()
        skuListToQuery.add(KEY_PREMIUM_ONE_MONTH)
        skuListToQuery.add(KEY_PREMIUM_ONE_YEAR)
        val skuDetailsParams = SkuDetailsParams.newBuilder()
            .setSkusList(skuListToQuery)
            .setType(BillingClient.SkuType.SUBS)
            .build()
        billingClient!!.querySkuDetailsAsync(
            skuDetailsParams
        ) { _, list ->
            if (list != null && list.isNotEmpty()) {
                skuDetailsList.addAll(list)
            }
        }
    }

    fun getSubscriptionById(id: String): SkuDetails? {
        try {
            if (!skuDetailsList.isNullOrEmpty()) {
                val iteratorList = skuDetailsList.iterator()
                while (iteratorList.hasNext()) {
                    val details = iteratorList.next()
                    if (details.sku == id) {
                        return details
                    }
                }
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
        return null
    }

    fun isPurchased(id: String, result: (Boolean) -> Unit) {
        if (billingClient == null || !billingClient!!.isReady) {
            result.invoke(false)
            return
        }
        billingClient!!.queryPurchasesAsync(BillingClient.SkuType.INAPP) { _, purchases ->
            if (purchases.isEmpty()) {
                result.invoke(false)
            } else {
                var isCheck = false
                for (purchase in purchases) {
                    if (purchase.skus.contains(id)) {
                        result.invoke(true)
                        isCheck = true
                        break
                    }
                }
                if (!isCheck) {
                    result.invoke(false)
                }
            }
        }
    }

    fun isSubscriptions(id: String, result: (Boolean) -> Unit) {
        if (billingClient == null || !billingClient!!.isReady) {
            result.invoke(false)
            return
        }
        billingClient!!.queryPurchasesAsync(BillingClient.SkuType.SUBS) { _, purchases ->
            if (purchases.isEmpty()) {
                result.invoke(false)
            } else {
                var isCheck = false
                for (purchase in purchases) {
                    if (purchase.skus.contains(id)) {
                        result.invoke(true)
                        isCheck = true
                        break
                    }
                }
                if (!isCheck) {
                    result.invoke(false)
                }
            }
        }

    }

    fun callPurchase(
        activity: Activity?,
        id: String,
        onCallComplete: () -> Unit
    ) {
        val skuListToQuery: MutableList<String> = java.util.ArrayList()
        skuListToQuery.add(id)
        val skuDetailsParams = SkuDetailsParams.newBuilder()
            .setSkusList(skuListToQuery)
            .setType(BillingClient.SkuType.INAPP)
            .build()
        billingClient?.querySkuDetailsAsync(skuDetailsParams,
            SkuDetailsResponseListener { _, list ->
                if (list != null && list.isNotEmpty()) {
                    skuDetailsList.addAll(list)
                    if (skuDetailsList.isEmpty()) {
                        return@SkuDetailsResponseListener
                    }
                    var skuDetails: SkuDetails? = null
                    for (details in list) {
                        if (details.sku == id) {
                            skuDetails = details
                        }
                    }
                    if (skuDetails == null) {
                        return@SkuDetailsResponseListener
                    }

                    // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetails)
                        .build()
                    val responseCode = billingClient!!.launchBillingFlow(
                        activity!!,
                        billingFlowParams
                    ).responseCode
                    if (responseCode == BillingClient.BillingResponseCode.OK) {
                        onCallComplete.invoke()
                    }
                }
            })
    }

    fun callSubscription(
        activity: Activity?,
        id: String,
        onCallComplete: () -> Unit
    ) {
        val skuListToQuery: MutableList<String> = java.util.ArrayList()
        skuListToQuery.add(id)
        val skuDetailsParams = SkuDetailsParams.newBuilder()
            .setSkusList(skuListToQuery)
            .setType(BillingClient.SkuType.SUBS)
            .build()
        billingClient!!.querySkuDetailsAsync(skuDetailsParams,
            SkuDetailsResponseListener { _, list ->
                if (list != null && list.isNotEmpty()) {
                    skuDetailsList.addAll(list)
                    if (skuDetailsList.isEmpty()) {
                        return@SkuDetailsResponseListener
                    }
                    var skuDetails: SkuDetails? = null
                    for (details in list) {
                        if (details.sku == id) {
                            skuDetails = details
                        }
                    }
                    if (skuDetails == null) {
                        return@SkuDetailsResponseListener
                    }
                    // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetails)
                        .build()
                    val responseCode = billingClient!!.launchBillingFlow(
                        activity!!,
                        billingFlowParams
                    ).responseCode
                    if (responseCode == BillingClient.BillingResponseCode.OK) {
                        onCallComplete.invoke()
                    }
                }
            })
    }

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { _: BillingResult?, purchases: List<Purchase?>? ->
            // To be implemented in a later section.
            if (purchases == null) {
                return@PurchasesUpdatedListener
            }
            for (purchase in purchases) {
                purchase?.let {
                    // mua hang thanh cong
                    handlePurchase(purchase)
                }
            }
        }

    private fun handlePurchase(purchase: Purchase) {
//        handleConsumableProduct(purchase);
        handleNonConsumableProduct(purchase)
    }

    private fun handleNonConsumableProduct(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient!!.acknowledgePurchase(
                    acknowledgePurchaseParams
                ) {
                    val sku = purchase.skus[0]
                    val skuDetail = skuDetailsList.firstOrNull { it.sku == sku }
                    if (skuDetail != null) {
//                        AppsFlyer.logPurchaseAppsFlyer(
//                            sku,
//                            skuDetail.type,
//                            skuDetail.priceAmountMicros,
//                            skuDetail.priceCurrencyCode
//                        )
                    }
                    //Handle acknowledge result
                    PreferencesUtils.putBoolean(Constants.PREFS_PURCHASED, true)
                }
            } else {
                PreferencesUtils.putBoolean(Constants.PREFS_PURCHASED, true)
            }
        } else {
//            AnalyticsManager.getInstance().postEvent(EventManager.IAPSubScr_RestoreFail_Show())
        }
    }
}