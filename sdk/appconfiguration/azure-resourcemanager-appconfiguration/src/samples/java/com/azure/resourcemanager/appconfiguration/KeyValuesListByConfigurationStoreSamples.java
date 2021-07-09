// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.appconfiguration;

import com.azure.core.util.Context;

/** Samples for KeyValues ListByConfigurationStore. */
public final class KeyValuesListByConfigurationStoreSamples {
    /**
     * Sample code: KeyValues_ListByConfigurationStore.
     *
     * @param appConfigurationManager Entry point to AppConfigurationManager.
     */
    public static void keyValuesListByConfigurationStore(
        com.azure.resourcemanager.appconfiguration.AppConfigurationManager appConfigurationManager) {
        appConfigurationManager.keyValues().listByConfigurationStore("myResourceGroup", "contoso", null, Context.NONE);
    }
}