// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.processor;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.spring.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.service.eventhubs.processor.EventHubsEventListenerContainerSupport;
import com.azure.spring.service.eventhubs.processor.EventHubsRecordEventMessageListener;
import com.azure.spring.service.eventhubs.processor.consumer.EventHubsInitializationContextConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.mock;

class DefaultEventHubsNamespaceProcessorFactoryTests {

    private EventHubsProcessorFactory processorFactory;
    private final String eventHubName = "eventHub";
    private final String consumerGroup = "group";
    private final String anotherConsumerGroup = "group2";
    private final EventHubsRecordEventMessageListener listener = eventContext -> { };
    private EventHubsEventListenerContainerSupport listenerContainerSupport;
    private int processorAddedTimes = 0;

    @BeforeEach
    void setUp() {
        NamespaceProperties namespaceProperties = new NamespaceProperties();
        namespaceProperties.setNamespace("test-namespace");
        this.processorFactory = new DefaultEventHubsNamespaceProcessorFactory(mock(CheckpointStore.class),
            namespaceProperties);
        processorAddedTimes = 0;
        this.processorFactory.addListener(new EventHubsProcessorFactory.Listener() {
            @Override
            public void processorAdded(String eventHub, String consumerGroup, EventProcessorClient client) {
                processorAddedTimes++;
            }
        });

        listenerContainerSupport = new EventHubsEventListenerContainerSupport() {
            @Override
            public EventHubsInitializationContextConsumer getInitializationContextConsumer() {
                return EventHubsEventListenerContainerSupport.super.getInitializationContextConsumer();
            }
        };
    }

    @Test
    void testGetEventProcessorClient() {
        EventProcessorClient processorClient = processorFactory.createProcessor(eventHubName, consumerGroup, listener, listenerContainerSupport);

        assertNotNull(processorClient);
        assertEquals(1, processorAddedTimes);
    }

    @Test
    void testCreateEventProcessorClientTwice() {
        EventProcessorClient client = processorFactory.createProcessor(eventHubName, consumerGroup, this.listener, listenerContainerSupport);
        assertNotNull(client);

        processorFactory.createProcessor(eventHubName, consumerGroup, this.listener, listenerContainerSupport);
        assertEquals(1, processorAddedTimes);
    }

    @Test
    void testRecreateEventProcessorClient() throws Exception {
        final EventProcessorClient client = processorFactory.createProcessor(eventHubName, consumerGroup, this.listener, listenerContainerSupport);
        assertNotNull(client);

        EventProcessorClient anotherClient = processorFactory.createProcessor(eventHubName, anotherConsumerGroup, this.listener, listenerContainerSupport);
        assertNotNull(anotherClient);
        assertEquals(2, processorAddedTimes);
    }

}
