package com.sample.android.classytaxijava.utils;

import static com.sample.android.classytaxijava.utils.SubscriptionUtilities.basicTextForSubscription;
import static com.sample.android.classytaxijava.utils.SubscriptionUtilities.premiumTextForSubscription;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import android.app.Application;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.sample.android.classytaxijava.data.SubscriptionStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class SubscriptionUtilitiesTest {

    private static final String BASIC_ON_HOLD = "Basic plan (on hold)";
    private static final String BASIC_PAUSED = "Basic plan (paused)";
    private static final String BASIC_ON_GRACE_PERIOD = "Basic plan (payment issue)";
    private static final String BASIC_SUB_RESTORE = "Basic plan (canceled)";
    private static final String BASIC_REGULAR_PURCHASE = "Basic plan (current)";
    private static final String BASIC_NOT_PURCHASED = "Basic plan";

    private static final String PREMIUM_ON_HOLD = "Premium plan (on hold)";
    private static final String PREMIUM_PAUSED = "Premium plan (paused)";
    private static final String PREMIUM_ON_GRACE_PERIOD = "Premium plan (payment issue)";
    private static final String PREMIUM_SUB_RESTORE = "Premium plan (canceled)";
    private static final String PREMIUM_REGULAR_PURCHASE = "Premium plan (current)";
    private static final String PREMIUM_NOT_PURCHASED = "Premium plan";

    private Application context;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void basicTextForSubscription_returnsBasicTextForOnHoldSub() {
        SubscriptionStatus onHoldSubscription = new SubscriptionStatus();
        onHoldSubscription.setSubAlreadyOwned(false);
        onHoldSubscription.setEntitlementActive(false);
        onHoldSubscription.setAccountHold(true);
        onHoldSubscription.setLocalPurchase(true);

        String result = basicTextForSubscription(context.getResources(), onHoldSubscription);
        assertThat(result, is(BASIC_ON_HOLD));
    }

    @Test
    public void basicTextForSubscription_returnsBasicTextForOnHoldAndNotOnDeviceSub() {
        SubscriptionStatus onHoldAndNonLocalSubscription = new SubscriptionStatus();
        onHoldAndNonLocalSubscription.setSubAlreadyOwned(false);
        onHoldAndNonLocalSubscription.setEntitlementActive(false);
        onHoldAndNonLocalSubscription.setAccountHold(true);
        onHoldAndNonLocalSubscription.setLocalPurchase(false);

        String result = basicTextForSubscription(context.getResources(), onHoldAndNonLocalSubscription);
        assertThat(result, is(BASIC_ON_HOLD + "*"));
    }

    @Test
    public void basicTextForSubscription_returnsBasicTextForPausedSub() {
        SubscriptionStatus pausedSubscription = new SubscriptionStatus();
        pausedSubscription.setSubAlreadyOwned(false);
        pausedSubscription.setEntitlementActive(false);
        pausedSubscription.setPaused(true);
        pausedSubscription.setLocalPurchase(true);

        String result = basicTextForSubscription(context.getResources(), pausedSubscription);
        assertThat(result, is(BASIC_PAUSED));

    }

    @Test
    public void basicTextForSubscription_returnsBasicTextForGracePeriodSub() {
        SubscriptionStatus onGraceSubscription = new SubscriptionStatus();
        onGraceSubscription.setSubAlreadyOwned(false);
        onGraceSubscription.setEntitlementActive(true);
        onGraceSubscription.setGracePeriod(true);
        onGraceSubscription.setLocalPurchase(true);

        String result = basicTextForSubscription(context.getResources(), onGraceSubscription);
        assertThat(result, is(BASIC_ON_GRACE_PERIOD));
    }

    @Test
    public void basicTextForSubscription_returnsBasicTextForSubRestore() {
        SubscriptionStatus willNotRenewSubscription = new SubscriptionStatus();
        willNotRenewSubscription.setSubAlreadyOwned(false);
        willNotRenewSubscription.setEntitlementActive(true);
        willNotRenewSubscription.setWillRenew(false);
        willNotRenewSubscription.setLocalPurchase(true);

        String result = basicTextForSubscription(context.getResources(), willNotRenewSubscription);
        assertThat(result, is(BASIC_SUB_RESTORE));
    }

    @Test
    public void basicTextForSubscription_returnsBasicTextForRegularSub() {
        SubscriptionStatus basicSkuSubscription = new SubscriptionStatus();
        basicSkuSubscription.setSubAlreadyOwned(false);
        basicSkuSubscription.setEntitlementActive(true);
        basicSkuSubscription.setSku("basic_subscription");
        basicSkuSubscription.setLocalPurchase(true);
        basicSkuSubscription.setWillRenew(true);

        String result = basicTextForSubscription(context.getResources(), basicSkuSubscription);
        assertThat(result, is(BASIC_REGULAR_PURCHASE));

    }
    @Test
    public void basicTextForSubscription_returnsBasicTextForNotPurchasedSub() {
        SubscriptionStatus nonBasicSkuSubscription = new SubscriptionStatus();
        nonBasicSkuSubscription.setSubAlreadyOwned(false);
        nonBasicSkuSubscription.setEntitlementActive(true);
        nonBasicSkuSubscription.setSku("premium_subscription");
        nonBasicSkuSubscription.setLocalPurchase(true);
        nonBasicSkuSubscription.setWillRenew(true);

        String result = basicTextForSubscription(context.getResources(), nonBasicSkuSubscription);
        assertThat(result, is(BASIC_NOT_PURCHASED));
    }

    @Test
    public void premiumTextForSubscription_returnsPremiumTextForOnHoldSub() {
        SubscriptionStatus onHoldSubscription = new SubscriptionStatus();
        onHoldSubscription.setSubAlreadyOwned(false);
        onHoldSubscription.setEntitlementActive(false);
        onHoldSubscription.setAccountHold(true);
        onHoldSubscription.setLocalPurchase(true);

        String result = premiumTextForSubscription(context.getResources(), onHoldSubscription);
        assertThat(result, is(PREMIUM_ON_HOLD));
    }

    @Test
    public void premiumTextForSubscription_returnsPremiumTextForOnHoldAndNotOnDeviceSub() {
        SubscriptionStatus onHoldAndNonLocalSubscription = new SubscriptionStatus();
        onHoldAndNonLocalSubscription.setSubAlreadyOwned(false);
        onHoldAndNonLocalSubscription.setEntitlementActive(false);
        onHoldAndNonLocalSubscription.setAccountHold(true);
        onHoldAndNonLocalSubscription.setLocalPurchase(false);

        String result = premiumTextForSubscription(context.getResources(), onHoldAndNonLocalSubscription);
        assertThat(result, is(PREMIUM_ON_HOLD + "*"));
    }

    @Test
    public void premiumTextForSubscription_returnsPremiumTextForPausedSub() {
        SubscriptionStatus pausedSubscription = new SubscriptionStatus();
        pausedSubscription.setSubAlreadyOwned(false);
        pausedSubscription.setEntitlementActive(false);
        pausedSubscription.setPaused(true);
        pausedSubscription.setLocalPurchase(true);

        String result = premiumTextForSubscription(context.getResources(), pausedSubscription);
        assertThat(result, is(PREMIUM_PAUSED));
    }

    @Test
    public void premiumTextForSubscription_returnsPremiumTextForGracePeriodSub() {
        SubscriptionStatus onGraceSubscription = new SubscriptionStatus();
        onGraceSubscription.setSubAlreadyOwned(false);
        onGraceSubscription.setEntitlementActive(true);
        onGraceSubscription.setGracePeriod(true);
        onGraceSubscription.setLocalPurchase(true);

        String result = premiumTextForSubscription(context.getResources(), onGraceSubscription);
        assertThat(result, is(PREMIUM_ON_GRACE_PERIOD));
    }

    @Test
    public void premiumTextForSubscription_returnsPremiumTextForSubRestore() {
        SubscriptionStatus willNotRenewSubscription = new SubscriptionStatus();
        willNotRenewSubscription.setSubAlreadyOwned(false);
        willNotRenewSubscription.setEntitlementActive(true);
        willNotRenewSubscription.setWillRenew(false);
        willNotRenewSubscription.setLocalPurchase(true);

        String result = premiumTextForSubscription(context.getResources(), willNotRenewSubscription);
        assertThat(result, is(PREMIUM_SUB_RESTORE));
    }

    @Test
    public void premiumTextForSubscription_returnsPremiumTextForRegularSub() {
        SubscriptionStatus premiumSkuSubscription = new SubscriptionStatus();
        premiumSkuSubscription.setSubAlreadyOwned(false);
        premiumSkuSubscription.setEntitlementActive(true);
        premiumSkuSubscription.setSku("premium_subscription");
        premiumSkuSubscription.setLocalPurchase(true);
        premiumSkuSubscription.setWillRenew(true);

        String result = premiumTextForSubscription(context.getResources(), premiumSkuSubscription);
        assertThat(result, is(PREMIUM_REGULAR_PURCHASE));
    }

    @Test
    public void premiumTextForSubscription_returnsPremiumTextForNotPurchasedSub() {
        SubscriptionStatus nonPremiumSkuSubscription = new SubscriptionStatus();
        nonPremiumSkuSubscription.setSubAlreadyOwned(false);
        nonPremiumSkuSubscription.setEntitlementActive(true);
        nonPremiumSkuSubscription.setSku("basic_subscription");
        nonPremiumSkuSubscription.setLocalPurchase(true);
        nonPremiumSkuSubscription.setWillRenew(true);

        String result = premiumTextForSubscription(context.getResources(), nonPremiumSkuSubscription);
        assertThat(result, is(PREMIUM_NOT_PURCHASED));
    }

}