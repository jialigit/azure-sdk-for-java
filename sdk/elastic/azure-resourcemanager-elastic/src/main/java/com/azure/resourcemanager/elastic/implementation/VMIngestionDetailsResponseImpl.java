// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.elastic.implementation;

import com.azure.resourcemanager.elastic.fluent.models.VMIngestionDetailsResponseInner;
import com.azure.resourcemanager.elastic.models.VMIngestionDetailsResponse;

public final class VMIngestionDetailsResponseImpl implements VMIngestionDetailsResponse {
    private VMIngestionDetailsResponseInner innerObject;

    private final com.azure.resourcemanager.elastic.ElasticManager serviceManager;

    VMIngestionDetailsResponseImpl(
        VMIngestionDetailsResponseInner innerObject, com.azure.resourcemanager.elastic.ElasticManager serviceManager) {
        this.innerObject = innerObject;
        this.serviceManager = serviceManager;
    }

    public String cloudId() {
        return this.innerModel().cloudId();
    }

    public String ingestionKey() {
        return this.innerModel().ingestionKey();
    }

    public VMIngestionDetailsResponseInner innerModel() {
        return this.innerObject;
    }

    private com.azure.resourcemanager.elastic.ElasticManager manager() {
        return this.serviceManager;
    }
}