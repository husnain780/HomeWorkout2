package com.homeworkout.app.service

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

class BillingManager(
    context: Context
) {

    companion object {
        const val PREMIUM_PRODUCT_ID =
            "homeworkout_premium"

        private const val PREFS =
            "homeworkout_billing"

        private const val PREMIUM_KEY =
            "premium_entitled"
    }

    private val preferences: SharedPreferences =
        context.applicationContext.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )

    private var productDetails:
        ProductDetails? = null

    private var listener:
        ((SubscriptionState) -> Unit)? = null

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener {
            billingResult,
            purchases ->

            when (billingResult.responseCode) {

                BillingResponseCode.OK -> {

                    purchases?.forEach {
                        processPurchase(it)
                    }
                }

                BillingResponseCode.USER_CANCELED -> {

                    publish(
                        SubscriptionState(
                            isReady = true,
                            isPremium =
                                isPremiumStored(),
                            message =
                                "Purchase cancelled."
                        )
                    )
                }

                else -> {

                    publish(
                        SubscriptionState(
                            isReady = true,
                            isPremium =
                                isPremiumStored(),
                            message =
                                billingResult.debugMessage
                                    .ifBlank {
                                        "Google Play billing is unavailable."
                                    }
                        )
                    )
                }
            }
        }

    private val billingClient =
        BillingClient.newBuilder(
            context.applicationContext
        )
            .setListener(
                purchasesUpdatedListener
            )
            .enablePendingPurchases(
                PendingPurchasesParams
                    .newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .enableAutoServiceReconnection()
            .build()

    fun setListener(
        callback: (SubscriptionState) -> Unit
    ) {
        listener = callback

        publish(
            SubscriptionState(
                isReady = billingClient.isReady,
                isPremium =
                    isPremiumStored(),
                message =
                    if (billingClient.isReady) {
                        "Ready"
                    } else {
                        "Connecting to Google Play..."
                    }
            )
        )
    }

    fun start() {

        if (billingClient.isReady) {
            queryProducts()
            queryExistingSubscription()
            return
        }

        billingClient.startConnection(
            object : com.android.billingclient.api.BillingClientStateListener {

                override fun onBillingSetupFinished(
                    billingResult: BillingResult
                ) {

                    if (
                        billingResult.responseCode ==
                        BillingResponseCode.OK
                    ) {

                        publish(
                            SubscriptionState(
                                isReady = true,
                                isPremium =
                                    isPremiumStored(),
                                message =
                                    "Google Play connected."
                            )
                        )

                        queryProducts()
                        queryExistingSubscription()

                    } else {

                        publish(
                            SubscriptionState(
                                isReady = false,
                                isPremium =
                                    isPremiumStored(),
                                message =
                                    billingResult.debugMessage
                            )
                        )
                    }
                }

                override fun onBillingServiceDisconnected() {

                    publish(
                        SubscriptionState(
                            isReady = false,
                            isPremium =
                                isPremiumStored(),
                            message =
                                "Google Play disconnected. Reconnecting automatically."
                        )
                    )
                }
            }
        )
    }

    private fun queryProducts() {

        val product =
            QueryProductDetailsParams.Product
                .newBuilder()
                .setProductId(
                    PREMIUM_PRODUCT_ID
                )
                .setProductType(
                    BillingClient.ProductType.SUBS
                )
                .build()

        val params =
            QueryProductDetailsParams
                .newBuilder()
                .setProductList(
                    listOf(product)
                )
                .build()

        billingClient.queryProductDetailsAsync(
            params
        ) { billingResult, result ->

            if (
                billingResult.responseCode ==
                BillingResponseCode.OK
            ) {

                productDetails =
                    result.productDetailsList
                        .firstOrNull()

                val details =
                    productDetails

                if (details == null) {

                    publish(
                        SubscriptionState(
                            isReady = true,
                            isPremium =
                                isPremiumStored(),
                            message =
                                "Premium subscription is not configured in Google Play yet."
                        )
                    )

                } else {

                    val offer =
                        details
                            .subscriptionOfferDetails
                            ?.firstOrNull()

                    val price =
                        offer
                            ?.pricingPhases
                            ?.pricingPhaseList
                            ?.firstOrNull()
                            ?.formattedPrice
                            ?: ""

                    publish(
                        SubscriptionState(
                            isReady = true,
                            isPremium =
                                isPremiumStored(),
                            productTitle =
                                details.title,
                            price = price,
                            message =
                                "Subscription available."
                        )
                    )
                }

            } else {

                publish(
                    SubscriptionState(
                        isReady = true,
                        isPremium =
                            isPremiumStored(),
                        message =
                            billingResult.debugMessage
                    )
                )
            }
        }
    }

    private fun queryExistingSubscription() {

        val params =
            QueryPurchasesParams
                .newBuilder()
                .setProductType(
                    BillingClient.ProductType.SUBS
                )
                .build()

        billingClient.queryPurchasesAsync(
            params
        ) { billingResult, purchases ->

            if (
                billingResult.responseCode ==
                BillingResponseCode.OK
            ) {

                val premiumPurchase =
                    purchases.firstOrNull { purchase ->

                        purchase.products.contains(
                            PREMIUM_PRODUCT_ID
                        ) &&
                            purchase.purchaseState ==
                            com.android.billingclient.api.Purchase
                                .PurchaseState
                                .PURCHASED
                    }

                if (premiumPurchase != null) {
                    processPurchase(
                        premiumPurchase
                    )
                } else {

                    preferences.edit()
                        .putBoolean(
                            PREMIUM_KEY,
                            false
                        )
                        .apply()

                    publish(
                        SubscriptionState(
                            isReady = true,
                            isPremium = false,
                            message =
                                "No active premium subscription found."
                        )
                    )
                }
            }
        }
    }

    private fun processPurchase(
        purchase:
            com.android.billingclient.api.Purchase
    ) {

        if (
            purchase.purchaseState !=
            com.android.billingclient.api.Purchase
                .PurchaseState
                .PURCHASED
        ) {
            return
        }

        if (
            !purchase.products.contains(
                PREMIUM_PRODUCT_ID
            )
        ) {
            return
        }

        preferences.edit()
            .putBoolean(
                PREMIUM_KEY,
                true
            )
            .apply()

        if (!purchase.isAcknowledged) {

            val params =
                AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(
                        purchase.purchaseToken
                    )
                    .build()

            billingClient.acknowledgePurchase(
                params
            ) { acknowledgementResult ->

                publish(
                    SubscriptionState(
                        isReady = true,
                        isPremium = true,
                        message =
                            if (
                                acknowledgementResult
                                    .responseCode ==
                                BillingResponseCode.OK
                            ) {
                                "Premium activated."
                            } else {
                                acknowledgementResult
                                    .debugMessage
                            }
                    )
                )
            }

        } else {

            publish(
                SubscriptionState(
                    isReady = true,
                    isPremium = true,
                    message =
                        "Premium is active."
                )
            )
        }
    }

    fun launchPurchase(
        activity: Activity
    ): BillingResult? {

        val details =
            productDetails
                ?: return null

        val offer =
            details
                .subscriptionOfferDetails
                ?.firstOrNull()
                ?: return null

        val productParams =
            BillingFlowParams
                .ProductDetailsParams
                .newBuilder()
                .setProductDetails(details)
                .setOfferToken(
                    offer.offerToken
                )
                .build()

        val flowParams =
            BillingFlowParams
                .newBuilder()
                .setProductDetailsParamsList(
                    listOf(productParams)
                )
                .build()

        return billingClient.launchBillingFlow(
            activity,
            flowParams
        )
    }

    fun isPremium(): Boolean {
        return isPremiumStored()
    }

    private fun isPremiumStored(): Boolean {
        return preferences.getBoolean(
            PREMIUM_KEY,
            false
        )
    }

    private fun publish(
        state: SubscriptionState
    ) {
        listener?.invoke(state)
    }

    fun close() {
        billingClient.endConnection()
    }
}
