package com.example.subscriptions.billing

import com.example.subscriptions.data.SubscriptionStatus
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.nullValue
import org.junit.Test


class BillingUtilitiesTest {

    @Test
    fun subscriptionForSku_returnsSubscriptionForExistingSku() {
        val subscriptionList = listOf(
            SubscriptionStatus(sku = "basic_subscription"),
            SubscriptionStatus(sku = "premium_subscription")
        )
        val sku = "basic_subscription"
        val result = subscriptionForSku(subscriptionList, sku)
        assertThat(sku, `is`(equalTo(result?.sku)))
    }

    @Test
    fun subscriptionForSku_returnsNullForNonExistingSku() {
        val noSkuSubscriptionList = listOf(
            SubscriptionStatus(sku = "basic_subscription"),
            SubscriptionStatus(sku = "premium_subscription")
        )
        val sku = "non_cheap_subscription"
        val result = subscriptionForSku(noSkuSubscriptionList, sku)
        assertThat(result, `is`(nullValue()))
    }

    @Test
    fun isGracePeriod_returnsTrueIfInGracePeriod() {
        val onGraceSubscription = SubscriptionStatus(
            subAlreadyOwned = false,
            isEntitlementActive = true,
            isGracePeriod = true
        )
        val result = isGracePeriod(onGraceSubscription)
        assertThat("Grace period is shown", result, `is`(equalTo(true)))
    }

    @Test
    fun isGracePeriod_returnsFalseIfNotInGracePeriod() {
        val notOnGraceSubscription = SubscriptionStatus(
            subAlreadyOwned = false,
            isEntitlementActive = true,
            isGracePeriod = false
        )
        val result = isGracePeriod(notOnGraceSubscription)
        assertThat("Grace period is not shown", result, `is`(equalTo(false)))
    }

    @Test
    fun isGracePeriod_returnsFalseIfAlreadyOwnedSub() {
        val alreadyOwnedSubscription = SubscriptionStatus(
            subAlreadyOwned = true,
            isEntitlementActive = true,
            isGracePeriod = true
        )
        val result = isGracePeriod(alreadyOwnedSubscription)
        assertThat("Grace period is not shown for already owned subscription", result, `is`(equalTo(false)))
    }

    @Test
    fun isGracePeriod_returnsFalseIfEntitlementInactive() {
        val entitlementInactiveSubscription = SubscriptionStatus(
            subAlreadyOwned = false,
            isEntitlementActive = false,
            isGracePeriod = true
        )
        val result = isGracePeriod(entitlementInactiveSubscription)
        assertThat("Grace period is not shown for inactive entitlement subscription", result, `is`(equalTo(false)))
    }

    @Test
    fun isGracePeriod_returnsFalseForNullSub() {
        val nullSubscription = null
        val result = isGracePeriod(nullSubscription)
        assertThat("Grace period is not shown due to null subscription", result, `is`(equalTo(false)))
    }

    @Test
    fun isSubscriptionRestore_returnsTrueForNonRenewingSub() {
        val willNotRenewSubscription = SubscriptionStatus(
            subAlreadyOwned = false,
            isEntitlementActive = true,
            willRenew = false
        )
        val result = isSubscriptionRestore(willNotRenewSubscription)
        assertThat("Restore option is shown for subscription that will not be renewed", result, `is`(equalTo(true)))
    }

    @Test
    fun isSubscriptionRestore_returnsFalseForRenewingSub() {
        val willRenewSubscription = SubscriptionStatus(
            subAlreadyOwned = false,
            isEntitlementActive = true,
            willRenew = true
        )
        val result = isSubscriptionRestore(willRenewSubscription)
        assertThat("Restore option is not shown for subscription that will be renewed", result, `is`(equalTo(false)))
    }

    @Test
    fun isSubscriptionRestore_returnsFalseForNullSub() {
        val nullSubscription = null
        val result = isSubscriptionRestore(nullSubscription)
        assertThat("Subscription restore is not shown due to null subscription", result, `is`(equalTo(false)))
    }

    @Test
    fun isBasicContent_returnsTrueForBasicSku() {
        val basicSkuSubscription = SubscriptionStatus(
            subAlreadyOwned = false,
            isEntitlementActive = true,
            sku = "basic_subscription"
        )
        val result = isBasicContent(basicSkuSubscription)
        assertThat("Basic content is shown", result, `is`(equalTo(true)))
    }

    @Test
    fun isBasicContent_returnsFalseForNonBasicSku() {
        val nonBasicSkuSubscription = SubscriptionStatus(
            subAlreadyOwned = false,
            isEntitlementActive = true,
            sku = "premium_subscription"
        )
        val result = isBasicContent(nonBasicSkuSubscription)
        assertThat("Basic content is not shown", result, `is`(equalTo(false)))
    }

    @Test
    fun isBasicContent_returnsFalseForNullSub() {
        val nullSubscription = null
        val result = isBasicContent(nullSubscription)
        assertThat("Basic content is not shown due to null subscription", result, `is`(equalTo(false)))
    }

    @Test
    fun isPremiumContent_returnsTrueForPremiumSku() {
        val premiumSkuSubscription = SubscriptionStatus(
            subAlreadyOwned = false,
            isEntitlementActive = true,
            sku = "premium_subscription"
        )
        val result = isPremiumContent(premiumSkuSubscription)
        assertThat("Premium content is shown", result, `is`(equalTo(true)))
    }

    @Test
    fun isPremiumContent_returnsFalseForNonPremiumSku() {
        val nonPremiumSkuSubscription = SubscriptionStatus(
            subAlreadyOwned = false,
            isEntitlementActive = true,
            sku = "basic_subscription"
        )
        val result = isPremiumContent(nonPremiumSkuSubscription)
        assertThat("Premium content is not shown", result, `is`(equalTo(false)))
    }

    @Test
    fun isPremiumContent_returnsFalseForNullSub() {
        val nullSubscription = null
        val result = isPremiumContent(nullSubscription)
        assertThat("Premium content is not shown due to null subscription", result, `is`(equalTo(false)))
    }

    @Test
    fun isAccountHold_returnsTrueForOnHoldSub() {
        val onHoldSubscription = SubscriptionStatus(
            subAlreadyOwned = false,
            isEntitlementActive = false,
            isAccountHold = true
        )
        val result = isAccountHold(onHoldSubscription)
        assertThat("Account hold is shown", result, `is`(equalTo(true)))
    }

    @Test
    fun isAccountHold_returnsFalseForNonHoldSub() {
        val notHoldSubscription = SubscriptionStatus(
            subAlreadyOwned = false,
            isEntitlementActive = true,
            isAccountHold = false
        )
        val result = isAccountHold(notHoldSubscription)
        assertThat("Account hold is not shown", result, `is`(equalTo(false)))
    }

    @Test
    fun isAccountHold_returnsFalseForNullSub() {
        val nullSubscription = null
        val result = isAccountHold(nullSubscription)
        assertThat("Account hold is not shown due to null subscription", result, `is`(equalTo(false)))
    }

    @Test
    fun isPaused_returnsTrueForPausedSub() {
        val pausedSubscription = SubscriptionStatus(
            subAlreadyOwned = false,
            isEntitlementActive = false,
            isPaused = true
        )
        val result = isPaused(pausedSubscription)
        assertThat("Account paused is shown", result, `is`(equalTo(true)))
    }

    @Test
    fun isPaused_returnsFalseForNonPausedSub() {
        val pausedSubscription = SubscriptionStatus(
            subAlreadyOwned = false,
            isEntitlementActive = true,
            isPaused = false
        )
        val result = isPaused(pausedSubscription)
        assertThat("Account paused is not shown", result, `is`(equalTo(false)))
    }

    @Test
    fun isPaused_returnsFalseForNullSub() {
        val nullSubscription = null
        val result = isPaused(nullSubscription)
        assertThat("Account paused is not shown due to null subscription", result, `is`(equalTo(false)))
    }

    @Test
    fun isTransferRequired_returnsTrueForAlreadyOwnedSub() {
        val alreadyOwnedSubscription = SubscriptionStatus(subAlreadyOwned = true)
        val result = isTransferRequired(alreadyOwnedSubscription)
        assertThat("Transfer is required due to sub being already owned", result, `is`(equalTo(true)))
    }

    @Test
    fun isTransferRequired_returnsFalseForNotAlreadyOwnedSub() {
        val notAlreadyOwnedSubscription = SubscriptionStatus(subAlreadyOwned = false)
        val result = isTransferRequired(notAlreadyOwnedSubscription)
        assertThat("Transfer is not required", result, `is`(equalTo(false)))
    }

    @Test
    fun isTransferRequired_returnsFalseForNullSub() {
        val nullSubscription = null
        val result = isTransferRequired(nullSubscription)
        assertThat("Transfer is not required due null sub", result, `is`(equalTo(false)))
    }
}