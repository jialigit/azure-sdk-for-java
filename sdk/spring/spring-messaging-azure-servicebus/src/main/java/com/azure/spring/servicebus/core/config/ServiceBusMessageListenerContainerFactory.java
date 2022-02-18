// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.config;

import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.messaging.container.AbstractMessageListenerContainer;
import com.azure.spring.messaging.config.BaseAzureMessageListenerContainerFactory;
import com.azure.spring.messaging.container.MessageListenerContainerFactory;
import com.azure.spring.servicebus.core.listener.ServiceBusMessageListenerContainer;

/**
 * A {@link MessageListenerContainerFactory} implementation to build a
 * standard {@link AbstractMessageListenerContainer}.
 *
 */
public class ServiceBusMessageListenerContainerFactory
        extends BaseAzureMessageListenerContainerFactory<AbstractMessageListenerContainer> {

    private final ServiceBusMessageListenerContainer serviceBusProcessorContainer;
    /**
     * Construct the listener container factory with the {@link ServiceBusMessageListenerContainer}.
     * @param serviceBusProcessorContainer the {@link ServiceBusMessageListenerContainer}.
     */
    public ServiceBusMessageListenerContainerFactory(ServiceBusMessageListenerContainer serviceBusProcessorContainer) {
        super();
        this.serviceBusProcessorContainer = serviceBusProcessorContainer;
    }

    @Override
    protected ServiceBusMessageListenerContainer createContainerInstance() {
        return new ServiceBusMessageListenerContainer(serviceBusProcessorContainer.getProcessorFactory() );
    }

}
