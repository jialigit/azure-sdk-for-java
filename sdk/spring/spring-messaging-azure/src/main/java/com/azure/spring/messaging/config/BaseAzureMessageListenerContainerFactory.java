// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.config;

import com.azure.spring.messaging.container.AbstractMessageListenerContainer;
import com.azure.spring.messaging.container.MessageListenerContainerFactory;
import com.azure.spring.messaging.endpoint.AbstractAzureListenerEndpoint;
import com.azure.spring.messaging.endpoint.AzureListenerEndpoint;

/**
 * Base {@link MessageListenerContainerFactory} for Spring's base container implementation.
 *
 * @param <C> the container type
 * @see AbstractAzureListenerEndpoint
 */
public abstract class BaseAzureMessageListenerContainerFactory<C extends AbstractMessageListenerContainer>
    implements MessageListenerContainerFactory<C> {

    @Override
    public C createListenerContainer(AzureListenerEndpoint endpoint) {
        C instance = createContainerInstance();
        initializeContainer(instance);
        endpoint.setupListenerContainer(instance);
        return instance;
    }

    /**
     * Create an empty container instance.
     *
     * @return C instance
     */
    protected abstract C createContainerInstance();

    /**
     * Further initialize the specified container.
     * <p>Subclasses can inherit from this method to apply extra
     * configuration if necessary.
     *
     * @param instance instance
     */
    protected void initializeContainer(C instance) {
    }

}
