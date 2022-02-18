// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.messaging;

import com.azure.spring.cloud.autoconfigure.eventhubs.AzureEventHubsMessagingAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusMessagingAutoConfiguration;
import com.azure.spring.eventhubs.core.config.EventHubsMessageListenerContainerFactory;
import com.azure.spring.eventhubs.core.listener.EventHubsMessageListenerContainer;
import com.azure.spring.messaging.annotation.EnableAzureMessaging;
import com.azure.spring.messaging.container.MessageListenerContainerFactory;
import com.azure.spring.messaging.container.MessageListenerContainer;
import com.azure.spring.servicebus.core.config.ServiceBusMessageListenerContainerFactory;
import com.azure.spring.servicebus.core.listener.ServiceBusMessageListenerContainer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for AzureMessagingListener.
 *
 * @since 4.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EnableAzureMessaging.class)
@Conditional(AzureMessagingListenerAutoConfiguration.MessagingListenerCondition.class)
@AutoConfigureAfter({
    AzureEventHubsMessagingAutoConfiguration.class,
    AzureServiceBusMessagingAutoConfiguration.class
})

public class AzureMessagingListenerAutoConfiguration {

    @Configuration
    @ConditionalOnBean(EventHubsMessageListenerContainer.class)
    static class EventHubsConfiguration {
         @Bean(name = "azureServiceBusListenerContainerFactory")
        @ConditionalOnMissingBean(name = "azureEventHubsListenerContainerFactory")
        public MessageListenerContainerFactory<? extends MessageListenerContainer> azureListenerContainerFactory(
            EventHubsMessageListenerContainer messageListenerContainer) {
            return new EventHubsMessageListenerContainerFactory(messageListenerContainer);
        }

    }

    @Configuration
    @ConditionalOnBean(ServiceBusMessageListenerContainer.class)
    static class ServiceBusConfiguration {
        @Bean(name = "azureServiceBusListenerContainerFactory")
        @ConditionalOnMissingBean(name = "azureServiceBusListenerContainerFactory")
        public MessageListenerContainerFactory<? extends MessageListenerContainer> azureServiceBusListenerContainerFactory(
            ServiceBusMessageListenerContainer serviceBusProcessorContainer) {
            return new ServiceBusMessageListenerContainerFactory(serviceBusProcessorContainer);
        }
    }


    static class MessagingListenerCondition extends AnyNestedCondition {

        public MessagingListenerCondition() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        public MessagingListenerCondition(ConfigurationPhase configurationPhase) {
            super(configurationPhase);
        }

        @ConditionalOnClass(EventHubsMessageListenerContainer.class)
        class ConditonalOnEventHubs {
        }

        @ConditionalOnClass(ServiceBusMessageListenerContainer.class)
        class ConditonalOnServiceBus {
        }


    }
}
