package com.sample.android.classytaxijava.billing;


import static com.sample.android.classytaxijava.billing.BillingUtilities.getSubscriptionForSku;
import static com.sample.android.classytaxijava.billing.BillingUtilities.isAccountHold;
import static com.sample.android.classytaxijava.billing.BillingUtilities.isBasicContent;
import static com.sample.android.classytaxijava.billing.BillingUtilities.isGracePeriod;
import static com.sample.android.classytaxijava.billing.BillingUtilities.isPaused;
import static com.sample.android.classytaxijava.billing.BillingUtilities.isPremiumContent;
import static com.sample.android.classytaxijava.billing.BillingUtilities.isSubscriptionRestore;
import static com.sample.android.classytaxijava.billing.BillingUtilities.isTransferRequired;

import com.sample.android.classytaxijava.data.SubscriptionStatus;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.List;

public class BillingUtilitiesTest {

    @Test
    public void subscriptionForSku_returnsSubscriptionForExistingSku() {
        SubscriptionStatus basicSub = new SubscriptionStatus();
        basicSub.setSku("basic_subscription");
        SubscriptionStatus premiumSub = new SubscriptionStatus();
        premiumSub.setSku("premium_subscription");

        List<SubscriptionStatus> noSkuSubscriptionList = new ArrayList<SubscriptionStatus>();
        noSkuSubscriptionList.add(basicSub);
        noSkuSubscriptionList.add(premiumSub);

        String sku = "basic_subscription";
        SubscriptionStatus result = getSubscriptionForSku(noSkuSubscriptionList, sku);
        assertThat(result.getSku(), is(sku));
    }

    @Test
    public void subscriptionForSku_returnsNullForNonExistingSku() {
        SubscriptionStatus basicSub = new SubscriptionStatus();
        basicSub.setSku("basic_subscription");
        SubscriptionStatus premiumSub = new SubscriptionStatus();
        premiumSub.setSku("premium_subscription");

        List<SubscriptionStatus> noSkuSubscriptionList = new ArrayList<SubscriptionStatus>();
        noSkuSubscriptionList.add(basicSub);
        noSkuSubscriptionList.add(premiumSub);

        String sku = "non_cheap_subscription";
        SubscriptionStatus result = getSubscriptionForSku(noSkuSubscriptionList, sku);
        assertThat(result, is(nullValue()));

    }

    @Test
    public void isGracePeriod_returnsTrueIfInGracePeriod() {
        SubscriptionStatus onGraceSubscription = new SubscriptionStatus();
        onGraceSubscription.setSubAlreadyOwned(false);
        onGraceSubscription.setEntitlementActive(true);
        onGraceSubscription.setGracePeriod(true);
        Boolean result = isGracePeriod(onGraceSubscription);
        assertThat(result, is(true));
    }

    @Test
    public void isGracePeriod_returnsFalseIfNotInGracePeriod() {
        SubscriptionStatus notOnGraceSubscription = new SubscriptionStatus();
        notOnGraceSubscription.setSubAlreadyOwned(false);
        notOnGraceSubscription.setEntitlementActive(true);
        notOnGraceSubscription.setGracePeriod(false);
        Boolean result = isGracePeriod(notOnGraceSubscription);
        assertThat(result, is(false));
    }

    @Test
    public void isGracePeriod_returnsFalseIfAlreadyOwnedSub() {
        SubscriptionStatus alreadyOwnedSubscription = new SubscriptionStatus();
        alreadyOwnedSubscription.setSubAlreadyOwned(true);
        alreadyOwnedSubscription.setEntitlementActive(true);
        alreadyOwnedSubscription.setGracePeriod(true);
        Boolean result = isGracePeriod(alreadyOwnedSubscription);
        assertThat(result, is(false));
    }

    @Test
    public void isGracePeriod_returnsFalseIfEntitlementInactive() {
        SubscriptionStatus entitlementInactiveSubscription = new SubscriptionStatus();
        entitlementInactiveSubscription.setSubAlreadyOwned(false);
        entitlementInactiveSubscription.setEntitlementActive(false);
        entitlementInactiveSubscription.setGracePeriod(true);
        Boolean result = isGracePeriod(entitlementInactiveSubscription);
        assertThat(result, is(false));
    }

    @Test
    public void isGracePeriod_returnsFalseForNullSub() {
        SubscriptionStatus nullSubscription = null;
        Boolean result = isGracePeriod(nullSubscription);
        assertThat(result, is(false));
    }

    @Test
    public void isSubscriptionRestore_returnsTrueForNonRenewingSub() {
        SubscriptionStatus willNotRenewSubscription = new SubscriptionStatus();
        willNotRenewSubscription.setSubAlreadyOwned(false);
        willNotRenewSubscription.setEntitlementActive(true);
        willNotRenewSubscription.setWillRenew(false);
        Boolean result = isSubscriptionRestore(willNotRenewSubscription);
        assertThat(result, is(true));

    }

    @Test
    public void isSubscriptionRestore_returnsFalseForRenewingSub() {
        SubscriptionStatus willRenewSubscription = new SubscriptionStatus();
        willRenewSubscription.setSubAlreadyOwned(false);
        willRenewSubscription.setEntitlementActive(true);
        willRenewSubscription.setWillRenew(true);
        Boolean result = isSubscriptionRestore(willRenewSubscription);
        assertThat(result, is(false));
    }

    @Test
    public void isSubscriptionRestore_returnsFalseForNullSub() {
        SubscriptionStatus nullSubscription = null;
        Boolean result = isSubscriptionRestore(nullSubscription);
        assertThat(result, is(false));
    }

    @Test
    public void isBasicContent_returnsTrueForBasicSku() {
        SubscriptionStatus basicSubscription = new SubscriptionStatus();
        basicSubscription.setSubAlreadyOwned(false);
        basicSubscription.setEntitlementActive(true);
        basicSubscription.setSku("basic_subscription");
        Boolean result = isBasicContent(basicSubscription);
        assertThat(result, is(true));
    }

    @Test
    public void isBasicContent_returnsFalseForNonBasicSku() {
        SubscriptionStatus nonBasicSubscription = new SubscriptionStatus();
        nonBasicSubscription.setSubAlreadyOwned(false);
        nonBasicSubscription.setEntitlementActive(true);
        nonBasicSubscription.setSku("premium_subscription");
        Boolean result = isBasicContent(nonBasicSubscription);
        assertThat(result, is(false));
    }

    @Test
    public void isBasicContent_returnsFalseForNullSub() {
        SubscriptionStatus nullSubscription = null;
        Boolean result = isBasicContent(nullSubscription);
        assertThat(result, is(false));
    }

    @Test
    public void isPremiumContent_returnsTrueForPremiumSku() {
        SubscriptionStatus premiumSubscription = new SubscriptionStatus();
        premiumSubscription.setSubAlreadyOwned(false);
        premiumSubscription.setEntitlementActive(true);
        premiumSubscription.setSku("premium_subscription");
        Boolean result = isPremiumContent(premiumSubscription);
        assertThat(result, is(true));
    }

    @Test
    public void isPremiumContent_returnsFalseForNonPremiumSku() {
        SubscriptionStatus nonPremiumSubscription = new SubscriptionStatus();
        nonPremiumSubscription.setSubAlreadyOwned(false);
        nonPremiumSubscription.setEntitlementActive(true);
        nonPremiumSubscription.setSku("basic_subscription");
        Boolean result = isPremiumContent(nonPremiumSubscription);
        assertThat(result, is(false));
    }

    @Test
    public void isPremiumContent_returnsFalseForNullSub() {
        SubscriptionStatus nullSubscription = null;
        Boolean result = isPremiumContent(nullSubscription);
        assertThat(result, is(false));
    }

    @Test
    public void isAccountHold_returnsTrueForOnHoldSub() {
        SubscriptionStatus onHoldSubscription = new SubscriptionStatus();
        onHoldSubscription.setSubAlreadyOwned(false);
        onHoldSubscription.setEntitlementActive(false);
        onHoldSubscription.setAccountHold(true);
        Boolean result = isAccountHold(onHoldSubscription);
        assertThat(result, is(true));
    }

    @Test
    public void isAccountHold_returnsFalseForNonHoldSub() {
        SubscriptionStatus notHoldSubscription = new SubscriptionStatus();
        notHoldSubscription.setSubAlreadyOwned(false);
        notHoldSubscription.setEntitlementActive(true);
        notHoldSubscription.setAccountHold(false);
        Boolean result = isAccountHold(notHoldSubscription);
        assertThat(result, is(false));
    }

    @Test
    public void isAccountHold_returnsFalseForNullSub() {
        SubscriptionStatus nullSubscription = null;
        Boolean result = isAccountHold(nullSubscription);
        assertThat(result, is(false));
    }

    @Test
    public void isPaused_returnsTrueForPausedSub() {
        SubscriptionStatus pausedSubscription = new SubscriptionStatus();
        pausedSubscription.setSubAlreadyOwned(false);
        pausedSubscription.setEntitlementActive(false);
        pausedSubscription.setPaused(true);
        Boolean result = isPaused(pausedSubscription);
        assertThat(result, is(true));
    }

    @Test
    public void isPaused_returnsFalseForNonPausedSub() {
        SubscriptionStatus nonPausedSubscription = new SubscriptionStatus();
        nonPausedSubscription.setSubAlreadyOwned(false);
        nonPausedSubscription.setEntitlementActive(true);
        nonPausedSubscription.setPaused(false);
        Boolean result = isPaused(nonPausedSubscription);
        assertThat(result, is(false));
    }

    @Test
    public void isPaused_returnsFalseForNullSub() {
        SubscriptionStatus nullSubscription = null;
        Boolean result = isPaused(nullSubscription);
        assertThat(result, is(false));
    }

    @Test
    public void isTransferRequired_returnsTrueForAlreadyOwnedSub() {
        SubscriptionStatus alreadyOwnedSubscription = new SubscriptionStatus();
        alreadyOwnedSubscription.setSubAlreadyOwned(true);
        Boolean result = isTransferRequired(alreadyOwnedSubscription);
        assertThat(result, is(true));
    }

    @Test
    public void isTransferRequired_returnsFalseForNotAlreadyOwnedSub() {
        SubscriptionStatus notAlreadyOwnedSubscription = new SubscriptionStatus();
        notAlreadyOwnedSubscription.setSubAlreadyOwned(false);
        Boolean result = isTransferRequired(notAlreadyOwnedSubscription);
        assertThat(result, is(false));
    }

    @Test
    public void isTransferRequired_returnsFalseForNullSub() {
        SubscriptionStatus nullSubscription = null;
        Boolean result = isTransferRequired(nullSubscription);
        assertThat(result, is(false));
    }
}