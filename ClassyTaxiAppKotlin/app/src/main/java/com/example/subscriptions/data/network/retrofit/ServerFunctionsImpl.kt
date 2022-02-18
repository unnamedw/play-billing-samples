/*
 * Copyright 2021 Google LLC. All rights reserved.
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

package com.example.subscriptions.data.network.retrofit

import android.util.Log
import com.example.subscriptions.BuildConfig.SERVER_URL
import com.example.subscriptions.data.ContentResource
import com.example.subscriptions.data.SubscriptionStatus
import com.example.subscriptions.data.SubscriptionStatusList
import com.example.subscriptions.data.network.firebase.ServerFunctions
import com.example.subscriptions.data.network.retrofit.authentication.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.HttpURLConnection

/**
 * Implementation of [ServerFunctions] using Retrofit.
 */
class ServerFunctionsImpl : ServerFunctions {

    private val retrofitClient = RetrofitClient(SERVER_URL, SubscriptionStatusApiCall::class.java)

    /**
     * Track the number of pending server requests.
     */
    private val pendingRequestCounter = PendingRequestCounter()

    private val _subscriptions = MutableStateFlow(emptyList<SubscriptionStatus>())
    private val _basicContent = MutableStateFlow<ContentResource?>(null)
    private val _premiumContent = MutableStateFlow<ContentResource?>(null)

    override val loading: StateFlow<Boolean> = pendingRequestCounter.loading
    override val subscriptions = _subscriptions.asStateFlow()
    override val basicContent = _basicContent.asStateFlow()
    override val premiumContent = _premiumContent.asStateFlow()

    override suspend fun updateBasicContent() {
        pendingRequestCounter.incrementRequestCount()
        val response = retrofitClient.getService().fetchBasicContent()
        _basicContent.emit(response)
        pendingRequestCounter.decrementRequestCount()
    }

    override suspend fun updatePremiumContent() {
        pendingRequestCounter.incrementRequestCount()
        val response = retrofitClient.getService().fetchPremiumContent()
        _premiumContent.emit(response)
        pendingRequestCounter.decrementRequestCount()
    }

    override suspend fun updateSubscriptionStatus() {
        pendingRequestCounter.incrementRequestCount()
        val response = retrofitClient.getService().fetchSubscriptionStatus()
        onSuccessfulSubscriptionCall(response, _subscriptions)
        pendingRequestCounter.decrementRequestCount()
    }

    /**
     * Register a subscription with the server and posts successful results to [subscriptions].
     */
    override suspend fun registerSubscription(sku: String, purchaseToken: String) {
        val data = SubscriptionStatus(
            sku = sku,
            purchaseToken = purchaseToken
        )
        pendingRequestCounter.incrementRequestCount()

        try {
            val response = retrofitClient.getService().registerSubscription(data)
            if (response.isSuccessful && response.body() != null) {
                response.body()?.let {
                    onSuccessfulSubscriptionCall(it, _subscriptions)
                } // ?: // TODO(b/219175303) handle error
            } else {
                if (response.code() == HttpURLConnection.HTTP_CONFLICT) {
                    Log.w(TAG, "Subscription already exists")
                    val oldSubscriptions = subscriptions.value
                    val newSubscription = newSub(sku, purchaseToken)
                    val newSubscriptions = insertOrUpdateSubscription(
                        oldSubscriptions, newSubscription
                    )
                    _subscriptions.emit(newSubscriptions)
                } else {
                    // TODO(b/219175303) handle error
                    Log.e(">>>", "registerSubscription() - ${response.code()}")
                }
            }

        } catch (e: Exception) {
            Log.w(">>>", "registerSubscription() - ${e.localizedMessage}")
        }

        pendingRequestCounter.decrementRequestCount()
    }

    /**
     * Transfer subscription to this account posts successful results to [subscriptions].
     */
    override suspend fun transferSubscription(sku: String, purchaseToken: String) {
        val data = SubscriptionStatus()
        data.sku = sku
        data.purchaseToken = purchaseToken
        pendingRequestCounter.incrementRequestCount()
        val response = retrofitClient.getService().transferSubscription(data)
        onSuccessfulSubscriptionCall(response, _subscriptions)
        pendingRequestCounter.decrementRequestCount()
    }

    /**
     * Register Instance ID when the user signs in or the token is refreshed.
     */
    override suspend fun registerInstanceId(instanceId: String) {
        val data = mapOf("instanceId" to instanceId)
        pendingRequestCounter.incrementRequestCount()
        retrofitClient.getService().registerInstanceID(data)
        // TODO(b/219175303) handle error
        pendingRequestCounter.decrementRequestCount()
    }

    /**
     * Unregister when the user signs out.
     */
    override suspend fun unregisterInstanceId(instanceId: String) {
        val data = mapOf("instanceId" to instanceId)
        pendingRequestCounter.incrementRequestCount()
        retrofitClient.getService().unregisterInstanceID(data)
        // TODO(b/219175303) handle error
        pendingRequestCounter.decrementRequestCount()
    }

    // Helper functions...

    /**
     * Inserts or updates the subscription to the list of existing com.example.subscriptions.
     *
     *
     * If none of the existing com.example.subscriptions have a SKU that matches, insert this SKU.
     * If a subscription exists with the matching SKU, the output list will contain the new
     * subscription instead of the old subscription.
     */
    private fun insertOrUpdateSubscription(
        oldSubscriptions: List<SubscriptionStatus>?,
        newSubscription: SubscriptionStatus
    ): List<SubscriptionStatus> {
        if (oldSubscriptions.isNullOrEmpty()) return listOf(newSubscription)
        return oldSubscriptions.filter { it.sku != newSubscription.sku } + newSubscription
    }

    /**
     * Called when a successful response returns from the server
     * for a [SubscriptionStatus] HTTPS call
     *
     * @param responseBody  Successful subscription statuses response object
     * @param subscriptions StateFlow subscription list
     */
    private suspend fun onSuccessfulSubscriptionCall(
        responseBody: SubscriptionStatusList,
        subscriptions: MutableStateFlow<List<SubscriptionStatus>>
    ) {
        val subs = responseBody.subscriptions.orEmpty()
        if (subs.isEmpty()) {
            Log.i(TAG, "No subscription data")
            return
        }
        Log.i(TAG, "Valid subscription data")
        subscriptions.emit(subs)
    }

    companion object {
        private const val TAG = "RemoteServerFunction"

        fun newSub(sku: String, purchaseToken: String): SubscriptionStatus =
            SubscriptionStatus.alreadyOwnedSubscription(sku, purchaseToken)
    }
}
