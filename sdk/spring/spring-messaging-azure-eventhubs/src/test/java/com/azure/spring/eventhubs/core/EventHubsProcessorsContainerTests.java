// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.spring.eventhubs.core.listener.EventHubsMessageListenerContainer;
import com.azure.spring.eventhubs.core.processor.EventHubsProcessorFactory;
import com.azure.spring.service.eventhubs.processor.EventHubsEventListenerContainerSupport;
import com.azure.spring.service.eventhubs.processor.EventHubsEventMessageListener;
import com.azure.spring.service.eventhubs.processor.EventHubsRecordEventMessageListener;
import com.azure.spring.service.eventhubs.processor.consumer.EventHubsInitializationContextConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventHubsProcessorsContainerTests {

    @Mock
    private EventHubsProcessorFactory mockProcessorFactory;

    @Mock
    private EventProcessorClient oneEventProcessorClient;

    @Mock
    private EventProcessorClient anotherEventProcessorClient;

    private EventHubsMessageListenerContainer processorContainer;
    private final EventHubsRecordEventMessageListener listener = eventContext -> {
    };
    private EventHubsEventListenerContainerSupport listenerContainerSupport;
    private AutoCloseable closeable;
    private final String consumerGroup = "consumer-group";
    private final String anotherConsumerGroup = "consumer-group2";
    private final String destination = "event-hub";


    @BeforeEach
    void setUp() {
        this.closeable = MockitoAnnotations.openMocks(this);
        when(this.mockProcessorFactory.createProcessor(eq(destination), eq(consumerGroup),
            isA(EventHubsEventMessageListener.class), isA(EventHubsEventListenerContainerSupport.class)))
            .thenReturn(this.oneEventProcessorClient);
        when(this.mockProcessorFactory.createProcessor(eq(destination), eq(anotherConsumerGroup),
            isA(EventHubsEventMessageListener.class), isA(EventHubsEventListenerContainerSupport.class)))
            .thenReturn(this.anotherEventProcessorClient);

        this.processorContainer = new EventHubsMessageListenerContainer(mockProcessorFactory);
        doNothing().when(this.oneEventProcessorClient).stop();
        doNothing().when(this.oneEventProcessorClient).start();

        listenerContainerSupport =
            new EventHubsEventListenerContainerSupport() {
                @Override
                public EventHubsInitializationContextConsumer getInitializationContextConsumer() {
                    return EventHubsEventListenerContainerSupport.super.getInitializationContextConsumer();
                }
            };
    }

    @AfterEach
    void close() throws Exception {
        closeable.close();
    }

    @Test
    void testSubscribe() {
        this.processorContainer.subscribe(this.destination, this.consumerGroup, this.listener, listenerContainerSupport);

        verifySubscriberCreatorCalled();
        verify(this.oneEventProcessorClient, times(1)).start();
    }

    @Test
    void testSubscribeTwice() {
        EventProcessorClient processorClient1 = this.processorContainer.subscribe(this.destination,
            this.consumerGroup, this.listener, listenerContainerSupport);
        verify(this.oneEventProcessorClient, times(1)).start();

        EventProcessorClient processorClient2 = this.processorContainer.subscribe(this.destination,
            this.consumerGroup, this.listener, listenerContainerSupport);

        Assertions.assertEquals(processorClient1, processorClient2);
        verifySubscriberCreatorCalled();
        verify(this.oneEventProcessorClient, times(2)).start();
    }

    @Test
    void testSubscribeWithAnotherGroup() {

        EventProcessorClient processorClient1 = this.processorContainer.subscribe(this.destination,
            this.consumerGroup, this.listener, listenerContainerSupport);
        verify(this.oneEventProcessorClient, times(1)).start();

        EventProcessorClient processorClient2 = this.processorContainer.subscribe(this.destination,
            this.anotherConsumerGroup, this.listener, listenerContainerSupport);
        Assertions.assertNotEquals(processorClient1, processorClient2);

        verifySubscriberCreatorCalled();
        verify(this.oneEventProcessorClient, times(1)).start();
        verify(this.anotherEventProcessorClient, times(1)).start();

    }

    private void verifySubscriberCreatorCalled() {
        verify(this.mockProcessorFactory, atLeastOnce()).createProcessor(anyString(), anyString(),
            isA(EventHubsEventMessageListener.class), isA(EventHubsEventListenerContainerSupport.class));
    }


}
