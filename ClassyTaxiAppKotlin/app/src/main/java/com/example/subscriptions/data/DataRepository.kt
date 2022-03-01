/*
 * Copyright 2018 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.subscriptions.data

import android.util.Log
import com.android.billingclient.api.Purchase
import com.example.subscriptions.Constants
import com.example.subscriptions.billing.BillingClientLifecycle
import com.example.subscriptions.data.disk.LocalDataSource
import com.example.subscriptions.data.network.WebDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Repository handling the work with subscriptions.
 */
class DataRepository private constructor(
    private val localDataSource: LocalDataSource,
    private val webDataSource: WebDataSource,
    private val billingClientLifecycle: BillingClientLifecycle,
    // TODO this should be injected externally
    private val externalScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {

    /**
     * True when there are pending network requests.
     */
    val loading: StateFlow<Boolean> = webDataSource.loading

    private val _subscriptions = MutableStateFlow<List<SubscriptionStatus>>(emptyList())
    /**
     * [MutableStateFlow] to coordinate updates from the database and the network.
     * Intended to be collected by ViewModel
     */
    val subscriptions = _subscriptions.asStateFlow()

    private val _basicContent = MutableStateFlow<ContentResource?>(null)
    /**
     * [StateFlow] with the basic content.
     * Intended to be collected by ViewModel
     */
    val basicContent = _basicContent.asStateFlow()

    private val _premiumContent = MutableStateFlow<ContentResource?>(null)
    /**
     * [StateFlow] with the premium content.
     * Intended to be collected by ViewModel
     */
    val premiumContent = _premiumContent.asStateFlow()

    init {
        // Update content from the web.
        // We are using a MutableStateFlow so that we can clear the data immediately
        // when the subscription changes.
        externalScope.launch {
            webDataSource.basicContent.collect {
                _basicContent.emit(it)
            }
        }
        externalScope.launch {
            webDataSource.premiumContent.collect {
                _premiumContent.emit(it)
            }
        }

        // Observed network changes are store in the database.
        // The database changes will propagate to the ViewModel.
        // We could write different logic to ensure that the network call completes when
        // the UI component is inactive.
        externalScope.launch {
            webDataSource.subscriptions.collect {
                Log.i(TAG, "webDataSource.subscriptions updated - ${it.size}")
                updateSubscriptionsFromNetwork(it)
            }
        }

        // When the list of purchases changes, we need to update the subscription status
        // to indicate whether the subscription is local or not. It is local if the
        // the Google Play Billing APIs return a Purchase record for the SKU. It is not
        // local if there is no record of the subscription on the device.
        billingClientLifecycle.purchases.observeForever { purchases ->
            // We only need to update the database if the isLocalPurchase field needs to change.
            subscriptions.value.let {
                val hasChanged = updateLocalPurchaseTokens(it, purchases)
                if (hasChanged) {
                    // FIXME(b/219175303) temporal impl.
                    externalScope.launch {
                        localDataSource.updateSubscriptions(it)
                        val updatedList = localDataSource.getSubscriptions()
                        Log.i(">>>", "billingClientLifecycle.purchases - ${updatedList.size}")
                        _subscriptions.emit(updatedList)
                    }
                }
            }
        }
    }

    fun updateSubscriptionsFromNetwork(remoteSubscriptions: List<SubscriptionStatus>?) {
        val oldSubscriptions = subscriptions.value
        val purchases = billingClientLifecycle.purchases.value
        val mergedSubscriptions =
            mergeSubscriptionsAndPurchases(oldSubscriptions, remoteSubscriptions, purchases)
        remoteSubscriptions?.let {
            acknowledgeRegisteredPurchaseTokens(it)
        }

        // Store the subscription information when it changes.
        // FIXME(b/219175303) temporal impl.
        externalScope.launch {
            localDataSource.updateSubscriptions(mergedSubscriptions)
            val updatedSubs = localDataSource.getSubscriptions()
            _subscriptions.emit(updatedSubs)
        }

        // Update the content when the subscription changes.
        remoteSubscriptions?.let {
            // Figure out which content we need to fetch.
            var updateBasic = false
            var updatePremium = false
            for (subscription in it) {
                when (subscription.sku) {
                    Constants.BASIC_SKU -> {
                        updateBasic = true
                    }
                    Constants.PREMIUM_SKU -> {
                        updatePremium = true
                        // Premium subscribers get access to basic content as well.
                        updateBasic = true
                    }
                }
            }

            // FIXME(b/219175303) temporal impl.
            externalScope.launch {
                if (updateBasic) {
                    // Fetch the basic content.
                    webDataSource.updateBasicContent()
                } else {
                    // If we no longer own this content, clear it from the UI.
                    _basicContent.emit(null)
                }
                if (updatePremium) {
                    // Fetch the premium content.
                    webDataSource.updatePremiumContent()
                } else {
                    // If we no longer own this content, clear it from the UI.
                    _premiumContent.emit(null)
                }
            }
        }
    }

    /**
     * Acknowledge subscriptions that have been registered by the server.
     */
    private fun acknowledgeRegisteredPurchaseTokens(
        remoteSubscriptions: List<SubscriptionStatus>
    ) {
        for (remoteSubscription in remoteSubscriptions) {
            remoteSubscription.purchaseToken?.let { purchaseToken ->
                billingClientLifecycle.acknowledgePurchase(purchaseToken)
            }
        }
    }

    /**
     * Merge the previous subscriptions and new subscriptions by looking at on-device purchases.
     *
     * We want to return the list of new subscriptions, possibly with some modifications
     * based on old subscriptions and the on-devices purchases from Google Play Billing.
     * Old subscriptions should be retained if they are owned by someone else (subAlreadyOwned)
     * and the purchase token for the subscription is still on this device.
     */
    private fun mergeSubscriptionsAndPurchases(
        oldSubscriptions: List<SubscriptionStatus>?,
        newSubscriptions: List<SubscriptionStatus>?,
        purchases: List<Purchase>?
    ): List<SubscriptionStatus> {
        return ArrayList<SubscriptionStatus>().apply {
            if (purchases != null) {
                // Record which purchases are local and can be managed on this device.
                updateLocalPurchaseTokens(newSubscriptions, purchases)
            }
            if (newSubscriptions != null) {
                addAll(newSubscriptions)
            }
            // Find old subscriptions that are in purchases but not in new subscriptions.
            if (purchases != null && oldSubscriptions != null) {
                for (oldSubscription in oldSubscriptions) {
                    if (oldSubscription.subAlreadyOwned && oldSubscription.isLocalPurchase) {
                        // This old subscription was previously marked as "already owned" by
                        // another user. It should be included in the output if the SKU
                        // and purchase token match their previous value.
                        for (purchase in purchases) {
                            if (purchase.skus[0] == oldSubscription.sku &&
                                purchase.purchaseToken == oldSubscription.purchaseToken
                            ) {
                                // The old subscription that was already owned subscription should
                                // be added to the new subscriptions.
                                // Look through the new subscriptions to see if it is there.
                                var foundNewSubscription = false
                                newSubscriptions?.let {
                                    for (newSubscription in it) {
                                        if (newSubscription.sku == oldSubscription.sku) {
                                            foundNewSubscription = true
                                        }
                                    }
                                }
                                if (!foundNewSubscription) {
                                    // The old subscription should be added to the output.
                                    // It matches a local purchase.
                                    add(oldSubscription)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Modify the subscriptions isLocalPurchase field based on the list of local purchases.
     * Return true if any of the values changed.
     */
    private fun updateLocalPurchaseTokens(
        subscriptions: List<SubscriptionStatus>?,
        purchases: List<Purchase>?
    ): Boolean {
        var hasChanged = false
        subscriptions?.forEach { subscription ->
            var isLocalPurchase = false
            var purchaseToken = subscription.purchaseToken
            purchases?.forEach { purchase ->
                if (subscription.sku == purchase.skus[0]) {
                    isLocalPurchase = true
                    purchaseToken = purchase.purchaseToken
                }
            }
            if (subscription.isLocalPurchase != isLocalPurchase) {
                subscription.isLocalPurchase = isLocalPurchase
                subscription.purchaseToken = purchaseToken
                hasChanged = true
            }
        }
        return hasChanged
    }

    /**
     * Fetch subscriptions from the server and update local data source.
     */
    fun fetchSubscriptions() {
        externalScope.launch {
            webDataSource.updateSubscriptionStatus()
        }
    }

    /**
     * Register subscription to this account and update local data source.
     */
    fun registerSubscription(sku: String, purchaseToken: String) {
        externalScope.launch {
            webDataSource.registerSubscription(sku = sku, purchaseToken = purchaseToken)
        }
    }

    /**
     * Transfer subscription to this account and update local data source.
     */
    fun transferSubscription(sku: String, purchaseToken: String) {
        externalScope.launch {
            webDataSource.postTransferSubscriptionSync(sku = sku, purchaseToken = purchaseToken)
        }
    }

    /**
     * Register Instance ID.
     */
    fun registerInstanceId(instanceId: String) {
        externalScope.launch {
            webDataSource.postRegisterInstanceId(instanceId)
        }
    }

    /**
     * Unregister Instance ID.
     */
    fun unregisterInstanceId(instanceId: String) {
        externalScope.launch {
            webDataSource.postUnregisterInstanceId(instanceId)
        }
    }

    /**
     * Delete local user data when the user signs out.
     */
    fun deleteLocalUserData() {
        externalScope.launch {
            localDataSource.deleteLocalUserData()
            _basicContent.emit(null)
            _premiumContent.emit(null)
        }
    }

    companion object {
        private const val TAG = "DataRepository"

        @Volatile
        private var INSTANCE: DataRepository? = null

        fun getInstance(
            localDataSource: LocalDataSource,
            webDataSource: WebDataSource,
            billingClientLifecycle: BillingClientLifecycle
        ): DataRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataRepository(localDataSource, webDataSource, billingClientLifecycle)
                    .also { INSTANCE = it }
            }
    }
}
