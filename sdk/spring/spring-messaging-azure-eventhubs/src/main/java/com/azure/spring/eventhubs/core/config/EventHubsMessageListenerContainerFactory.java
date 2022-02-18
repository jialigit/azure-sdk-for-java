// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.config;

import com.azure.spring.eventhubs.core.listener.EventHubsMessageListenerContainer;
import com.azure.spring.messaging.config.BaseAzureMessageListenerContainerFactory;
import com.azure.spring.messaging.container.AbstractMessageListenerContainer;
import com.azure.spring.messaging.container.MessageListenerContainer;
import com.azure.spring.messaging.container.MessageListenerContainerFactory;

/**
 * A {@link MessageListenerContainerFactory} implementation to build a standard {@link MessageListenerContainer}.
 */
public class EventHubsMessageListenerContainerFactory
    extends BaseAzureMessageListenerContainerFactory<AbstractMessageListenerContainer> {

    private final EventHubsMessageListenerContainer eventHubsProcessorContainer;

    /**
     * Construct the listener container factory with the {@link EventHubsMessageListenerContainer}.
     *
     * @param eventHubsProcessorContainer the {@link EventHubsMessageListenerContainer}.
     */
    public EventHubsMessageListenerContainerFactory(EventHubsMessageListenerContainer eventHubsProcessorContainer) {
        super();
        this.eventHubsProcessorContainer = eventHubsProcessorContainer;
    }

    @Override
    protected EventHubsMessageListenerContainer createContainerInstance() {
        return new EventHubsMessageListenerContainer(eventHubsProcessorContainer.getProcessorFactory());
    }

}
