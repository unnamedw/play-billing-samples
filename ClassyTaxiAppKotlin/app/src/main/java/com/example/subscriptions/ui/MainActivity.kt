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

package com.example.subscriptions.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModelProvider
import com.android.billingclient.api.Purchase
import com.example.subscriptions.Constants
import com.example.subscriptions.SubApp
import com.example.subscriptions.billing.BillingClientLifecycle
import com.example.subscriptions.ui.composable.home.ClassyTaxiApp
import com.firebase.ui.auth.AuthUI


/**
 * MainActivity contains a UI that leverages Material Components to build an optimized
 * Android experience for Classy Taxi.
 *
 * Uses [FirebaseUserViewModel] to maintain authentication state.
 * The menu is updated when the [FirebaseUser] changes.
 * When sign-in or sign-out is completed, call the [FirebaseUserViewModel] to update the state.
 *
 */
class MainActivity : AppCompatActivity() {
    private lateinit var billingClientLifecycle: BillingClientLifecycle

    private lateinit var authenticationViewModel: FirebaseUserViewModel
    private lateinit var billingViewModel: BillingViewModel
    private lateinit var subscriptionViewModel: SubscriptionStatusViewModel

    private val registerResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            Log.d(TAG, "Sign-in SUCCESS!")
            authenticationViewModel.updateFirebaseUser()
        } else {
            Log.d(TAG, "Sign-in FAILED!")
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        authenticationViewModel = ViewModelProvider(this)[FirebaseUserViewModel::class.java]
        billingViewModel = ViewModelProvider(this)[BillingViewModel::class.java]
        subscriptionViewModel =
            ViewModelProvider(this)[SubscriptionStatusViewModel::class.java]

        // Billing APIs are all handled in the this lifecycle observer.
        billingClientLifecycle = (application as SubApp).billingClientLifecycle
        lifecycle.addObserver(billingClientLifecycle)

        // Launch the billing flow when the user clicks a button to buy something.
        billingViewModel.buyEvent.observe(this) {
            if (it != null) {
                billingClientLifecycle.launchBillingFlow(this, it)
            }
        }

        // Open the Play Store when this event is triggered.
        billingViewModel.openPlayStoreSubscriptionsEvent.observe(this) { product ->
            Log.i(TAG, "Viewing subscriptions on the Google Play Store")
            val url = if (product == null) {
                // If the Product is not specified, just open the Google Play subscriptions URL.
                Constants.PLAY_STORE_SUBSCRIPTION_URL
            } else {
                // If the Product is specified, open the deeplink for this Product on Google Play.
                String.format(Constants.PLAY_STORE_SUBSCRIPTION_DEEPLINK_URL, product, packageName)
            }
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        // Update authentication UI.
        authenticationViewModel.firebaseUser.observe(this) {
            invalidateOptionsMenu()
            if (it == null) {
                triggerSignIn()
            } else {
                Log.d(TAG, "CURRENT user: ${it.email} ${it.displayName}")
            }
        }

        // Update subscription information when user changes.
        authenticationViewModel.userChangeEvent.observe(this) {
            subscriptionViewModel.userChanged()
            registerPurchases(billingClientLifecycle.purchases.value)
        }

        super.onCreate(savedInstanceState)
        setContent {
            ClassyTaxiApp(
                billingViewModel = billingViewModel,
                subscriptionViewModel = subscriptionViewModel,
                billingClientLifecycle = billingClientLifecycle,
                authenticationViewModel = authenticationViewModel,
            )
        }
    }


    /**
     * Register Products and purchase tokens with the server.
     */
    private fun registerPurchases(purchaseList: List<Purchase>) {
        for (purchase in purchaseList) {
            val product = purchase.products[0]
            val purchaseToken = purchase.purchaseToken
            Log.d(TAG, "Register purchase with product: $product, token: $purchaseToken")
            subscriptionViewModel.registerSubscription(
                product = product,
                purchaseToken = purchaseToken
            )
        }
    }

    /**
     * Sign in with FirebaseUI Auth.
     */
    private fun triggerSignIn() {
        Log.d(TAG, "Attempting SIGN-IN!")
        val providers = listOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        registerResult.launch(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
        )
    }


    companion object {
        private const val TAG = "MainActivity"
    }
}
