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

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.example.subscriptions.Constants
import com.example.subscriptions.R
import com.example.subscriptions.billing.isAccountHold
import com.example.subscriptions.billing.isBasicContent
import com.example.subscriptions.billing.isGracePeriod
import com.example.subscriptions.billing.isPaused
import com.example.subscriptions.billing.isPremiumContent
import com.example.subscriptions.billing.isSubscriptionRestore
import com.example.subscriptions.billing.isTransferRequired
import com.example.subscriptions.data.ContentResource
import com.example.subscriptions.data.SubscriptionStatus
import com.example.subscriptions.utils.basicTextForSubscription
import com.example.subscriptions.utils.premiumTextForSubscription
import java.text.SimpleDateFormat
import java.util.Calendar

private const val TAG = "BindingAdapter"

/**
 * Update a loading progress bar when the status changes.
 *
 * When the network state changes, the binding adapter triggers this view in the layout XML.
 * See the layout XML files for the app:loadingProgressBar attribute.
 */
@BindingAdapter("loadingProgressBar")
fun ProgressBar.loadingProgressBar(loading: Boolean?) {
    visibility = if (loading == true) View.VISIBLE else View.GONE
}

/**
 * Load image with original size. If url is null, hide [ImageView]
 */
@BindingAdapter("loadImageOrHide")
fun ImageView.loadImageOrHide(url: String?) {
    if (url != null) {
        Log.d(TAG, "Loading image for basic content: $url")
        visibility = View.VISIBLE
        Glide.with(context)
            .load(url)
            .override(Target.SIZE_ORIGINAL)
            .into(this)
    } else {
        visibility = View.GONE
    }
}

/**
 * Update basic content when the URL changes.
 */
@BindingAdapter("basicContent")
fun TextView.updateBasicContent(basicContent: ContentResource?) {
    text = if (basicContent?.url != null)
        resources.getString(R.string.basic_message)
    else
        resources.getString(R.string.no_basic_content)
}

/**
 * Update premium content on the Premium fragment when the URL changes.
 */
@BindingAdapter("premiumContent")
fun TextView.updatePremiumContent(premiumContent: ContentResource?) {
    text = if (premiumContent?.url != null)
        resources.getString(R.string.premium_content_text)
    else
        resources.getString(R.string.no_premium_content)
}

/**
 * Update subscription views on the Home fragment when the subscription changes.
 *
 * When the subscription changes, the binding adapter triggers this view in the layout XML.
 * See the layout XML files for the app:updateHomeViews attribute.
 */
@BindingAdapter("updateHomeViews")
fun LinearLayout.updateHomeViews(subscriptions: List<SubscriptionStatus>?) {
    val paywallMessage = findViewById<View>(R.id.home_paywall_message)
    val restoreMessage = findViewById<TextView>(R.id.home_restore_message)
    val gracePeriodMessage = findViewById<View>(R.id.home_grace_period_message)
    val transferMessage = findViewById<View>(R.id.home_transfer_message)
    val accountHoldMessage = findViewById<View>(R.id.home_account_hold_message)
    val accountPausedMessage = findViewById<View>(R.id.home_account_paused_message)
    val basicMessage = findViewById<View>(R.id.home_basic_message)
    val accountPausedMessageText = findViewById<TextView>(R.id.home_account_paused_message_text)

    // Set visibility assuming no subscription is available.
    // If a subscription is found that meets certain criteria, then the visibility of the paywall
    // will be changed to View.GONE.
    paywallMessage.visibility = View.VISIBLE
    // The remaining views start hidden. If a subscription is found that meets each criteria,
    // then the visibility will be changed to View.VISIBLE.
    listOf(
        restoreMessage, gracePeriodMessage, transferMessage, accountHoldMessage,
        accountPausedMessage, basicMessage
    ).forEach { it.visibility = View.GONE }

    // Update based on subscription information.
    subscriptions?.forEach { subscription ->
        if (isSubscriptionRestore(subscription)) {
            Log.d(TAG, "restore VISIBLE")
            restoreMessage.run {
                visibility = View.VISIBLE
                val expiryDate = getHumanReadableDate(subscription.activeUntilMillisec)
                text = resources.getString(R.string.restore_message_with_date, expiryDate)
            }
            paywallMessage.visibility = View.GONE // Paywall gone.
        }
        if (isGracePeriod(subscription)) {
            Log.d(TAG, "grace period VISIBLE")
            gracePeriodMessage.visibility = View.VISIBLE
            paywallMessage.visibility = View.GONE // Paywall gone.
        }
        if (isTransferRequired(subscription) && subscription.sku == Constants.BASIC_SKU) {
            Log.d(TAG, "transfer VISIBLE")
            transferMessage.visibility = View.VISIBLE
            paywallMessage.visibility = View.GONE // Paywall gone.
        }
        if (isAccountHold(subscription)) {
            Log.d(TAG, "account hold VISIBLE")
            accountHoldMessage.visibility = View.VISIBLE
            paywallMessage.visibility = View.GONE // Paywall gone.
        }
        if (isPaused(subscription)) {
            Log.d(TAG, "account paused VISIBLE")
            accountPausedMessageText.run {
                val autoResumeDate = getHumanReadableDate(subscription.autoResumeTimeMillis)
                text = resources.getString(R.string.account_paused_message, autoResumeDate)
            }
            accountPausedMessage.visibility = View.VISIBLE
            paywallMessage.visibility = View.GONE // Paywall gone.
        }
        if (isBasicContent(subscription) || isPremiumContent(subscription)) {
            Log.d(TAG, "basic VISIBLE")
            basicMessage.visibility = View.VISIBLE
            paywallMessage.visibility = View.GONE // Paywall gone.
        }
    }
}

/**
 * Update subscription views on the Premium fragment when the subscription changes.
 *
 * When the subscription changes, the binding adapter triggers this view in the layout XML.
 * See the layout XML files for the app:updatePremiumViews attribute.
 */
@BindingAdapter("updatePremiumViews")
fun LinearLayout.updatePremiumViews(subscriptions: List<SubscriptionStatus>?) {
    val paywallMessage = findViewById<View>(R.id.premium_paywall_message)
    val restoreMessage = findViewById<TextView>(R.id.premium_restore_message)
    val gracePeriodMessage = findViewById<TextView>(R.id.premium_grace_period_message)
    val transferMessage = findViewById<View>(R.id.premium_transfer_message)
    val accountHoldMessage = findViewById<View>(R.id.premium_account_hold_message)
    val accountPausedMessage = findViewById<View>(R.id.premium_account_paused_message)
    val premiumContent = findViewById<View>(R.id.premium_premium_content)
    val upgradeMessage = findViewById<View>(R.id.premium_upgrade_message)
    val accountPausedMessageText = findViewById<TextView>(R.id.premium_account_paused_message_text)

    // Set visibility assuming no subscription is available.
    // If a subscription is found that meets certain criteria, then the visibility of the paywall
    // will be changed to View.GONE.
    paywallMessage.visibility = View.VISIBLE
    // The remaining views start hidden. If a subscription is found that meets each criteria,
    // then the visibility will be changed to View.VISIBLE.
    listOf(
        restoreMessage, gracePeriodMessage, transferMessage, accountHoldMessage,
        accountPausedMessage, premiumContent, upgradeMessage
    ).forEach { it.visibility = View.GONE }

    // The Upgrade button should appear if the user has a basic subscription, but does not
    // have a premium subscription. This variable keeps track of whether a premium subscription
    // has been found when looking through the list of subscriptions.
    var hasPremium = false
    // Update based on subscription information.
    subscriptions?.let {
        for (subscription in subscriptions) {
            if (isSubscriptionRestore(subscription)) {
                Log.d(TAG, "restore VISIBLE")
                restoreMessage.run {
                    visibility = View.VISIBLE
                    val expiryDate = getHumanReadableDate(subscription.activeUntilMillisec)
                    text = resources.getString(R.string.restore_message_with_date, expiryDate)
                }
                paywallMessage.visibility = View.GONE // Paywall gone.
            }
            if (isGracePeriod(subscription)) {
                Log.d(TAG, "grace period VISIBLE")
                gracePeriodMessage.visibility = View.VISIBLE
                paywallMessage.visibility = View.GONE // Paywall gone.
            }
            if (isTransferRequired(subscription) && subscription.sku == Constants.PREMIUM_SKU) {
                Log.d(TAG, "transfer VISIBLE")
                transferMessage.visibility = View.VISIBLE
                paywallMessage.visibility = View.GONE // Paywall gone.
            }
            if (isAccountHold(subscription)) {
                Log.d(TAG, "account hold VISIBLE")
                accountHoldMessage.visibility = View.VISIBLE
                paywallMessage.visibility = View.GONE // Paywall gone.
            }
            if (isPaused(subscription)) {
                Log.d(TAG, "account paused VISIBLE")
                accountPausedMessageText.run {
                    val autoResumeDate = getHumanReadableDate(subscription.autoResumeTimeMillis)
                    text = resources.getString(R.string.account_paused_message, autoResumeDate)
                }
                accountPausedMessage.visibility = View.VISIBLE
                paywallMessage.visibility = View.GONE // Paywall gone.
            }

            // The upgrade message must be shown if there is a basic subscription
            // and there are zero premium subscriptions. We need to keep track of the premium
            // subscriptions and hide the upgrade message if we find any.
            if (isPremiumContent(subscription)) {
                Log.d(TAG, "premium VISIBLE")
                premiumContent.visibility = View.VISIBLE
                paywallMessage.visibility = View.GONE // Paywall gone.
                // Make sure we do not ask for an upgrade when user has premium subscription.
                hasPremium = true
                upgradeMessage.visibility = View.GONE
            }
            if (subscription.isBasicContent && !isPremiumContent(subscription) && !hasPremium) {
                Log.d(TAG, "basic VISIBLE")
                // Upgrade message will be hidden if a premium subscription is found later.
                upgradeMessage.visibility = View.VISIBLE
                paywallMessage.visibility = View.GONE // Paywall gone.
            }
        }
    }
}

/**
 * Update views on the Settings fragment when the subscription changes.
 *
 * When the subscription changes, the binding adapter triggers this view in the layout XML.
 * See the layout XML files for the app:updateSettingsViews attribute.
 */
@BindingAdapter("updateSettingsViews")
fun LinearLayout.updateSettingsViews(subscriptions: List<SubscriptionStatus>?) {
    val premiumButton = findViewById<Button>(R.id.subscription_option_premium_button)
    val basicButton = findViewById<Button>(R.id.subscription_option_basic_button)
    val settingsTransferMessage = findViewById<View>(R.id.settings_transfer_message)
    val settingsTransferMessageText = findViewById<TextView>(R.id.settings_transfer_message_text)

    // Set default button text: it might be overridden based on the subscription state.
    premiumButton.text = resources.getString(R.string.subscription_option_premium_message)
    basicButton.text = resources.getString(R.string.subscription_option_basic_message)

    // Update based on subscription information.
    var basicRequiresTransfer = false
    var premiumRequiresTransfer = false
    subscriptions?.forEach { subscription ->
        when (subscription.sku) {
            Constants.BASIC_SKU -> {
                basicButton.text =
                    basicTextForSubscription(resources, subscription)
                if (isTransferRequired(subscription)) {
                    basicRequiresTransfer = true
                }
            }
            Constants.PREMIUM_SKU -> {
                premiumButton.text =
                    premiumTextForSubscription(resources, subscription)
                if (isTransferRequired(subscription)) {
                    premiumRequiresTransfer = true
                }
            }
        }
    }
    val message = when {
        basicRequiresTransfer && premiumRequiresTransfer -> {
            val basicName = resources.getString(R.string.basic_button_text)
            val premiumName = resources.getString(R.string.premium_button_text)
            resources.getString(R.string.transfer_message_with_two_skus, basicName, premiumName)
        }
        basicRequiresTransfer -> {
            val basicName = resources.getString(R.string.basic_button_text)
            resources.getString(R.string.transfer_message_with_sku, basicName)
        }
        premiumRequiresTransfer -> {
            val premiumName = resources.getString(R.string.premium_button_text)
            resources.getString(R.string.transfer_message_with_sku, premiumName)
        }
        else -> null
    }
    if (message != null) {
        Log.d(TAG, "transfer VISIBLE")
        settingsTransferMessage.visibility = View.VISIBLE
        settingsTransferMessageText.text = message
    } else {
        settingsTransferMessage.visibility = View.GONE
        settingsTransferMessageText.text = resources.getString(R.string.transfer_message)
    }
}

/**
 * Get a readable date from the time in milliseconds.
 */
fun getHumanReadableDate(milliSeconds: Long): String {
    val formatter = SimpleDateFormat.getDateInstance()
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = milliSeconds
    if (milliSeconds == 0L) {
        Log.d(TAG, "Suspicious time: 0 milliseconds.")
    } else {
        Log.d(TAG, "Milliseconds: $milliSeconds")
    }
    return formatter.format(calendar.time)
}
