// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.consumption;

import com.azure.core.util.Context;

/** Samples for ReservationRecommendations List. */
public final class ReservationRecommendationsListSamples {
    /**
     * Sample code: ReservationRecommendationsBySubscription-Legacy.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void reservationRecommendationsBySubscriptionLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .reservationRecommendations()
            .list("subscriptions/00000000-0000-0000-0000-000000000000", null, Context.NONE);
    }

    /**
     * Sample code: ReservationRecommendationsByBillingProfile-Modern.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void reservationRecommendationsByBillingProfileModern(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .reservationRecommendations()
            .list("providers/Microsoft.Billing/billingAccounts/123456/billingProfiles/6420", null, Context.NONE);
    }

    /**
     * Sample code: ReservationRecommendationsByResourceGroup-Legacy.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void reservationRecommendationsByResourceGroupLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .reservationRecommendations()
            .list("subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testGroup", null, Context.NONE);
    }

    /**
     * Sample code: ReservationRecommendationsFilterBySubscriptionForScopeLookBackPeriod-Legacy.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void reservationRecommendationsFilterBySubscriptionForScopeLookBackPeriodLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .reservationRecommendations()
            .list(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                "properties/scope eq 'Single' AND properties/lookBackPeriod eq 'Last7Days'",
                Context.NONE);
    }

    /**
     * Sample code: ReservationRecommendationsByBillingAccount-Legacy.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void reservationRecommendationsByBillingAccountLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .reservationRecommendations()
            .list("providers/Microsoft.Billing/billingAccounts/123456", null, Context.NONE);
    }
}