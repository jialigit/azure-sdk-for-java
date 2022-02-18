// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.listener;

import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.spring.eventhubs.core.processor.EventHubsProcessorFactory;
import com.azure.spring.messaging.ConsumerIdentifier;
import com.azure.spring.messaging.container.AbstractMessageListenerContainer;
import com.azure.spring.service.eventhubs.processor.EventHubsEventListenerContainerSupport;
import com.azure.spring.service.eventhubs.processor.EventHubsEventMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.Lifecycle;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * EventHubs message listener container using {@link EventProcessorClient} to subscribe to event hubs and consume events
 * from all the partitions of each event hub.
 *
 * <p>
 * For different combinations of event hubs instance and consumer group, different {@link EventProcessorClient}s will be
 * created to subscribe to it.
 * </p>
 * <p>
 * Implementation of {@link AbstractMessageListenerContainer} is required when using {@link
 * EventProcessorClient} to consume events.
 *
 * @see AbstractMessageListenerContainer
 */
public class EventHubsMessageListenerContainer extends AbstractMessageListenerContainer implements Lifecycle,
    DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubsMessageListenerContainer.class);

    private final EventHubsProcessorFactory processorFactory;
    private final Map<ConsumerIdentifier, EventProcessorClient> clients = new HashMap<>();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    /**
     * Create an instance using the supplied processor factory.
     *
     * @param processorFactory the processor factory.
     */
    public EventHubsMessageListenerContainer(EventHubsProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }

    public EventHubsProcessorFactory getProcessorFactory() {
        return processorFactory;
    }

    @Override
    public void destroy() {
        stop();
    }

    @Override
    protected void doStart() {
        this.clients.values().forEach(EventProcessorClient::start);
    }

    @Override
    protected void doStop() {
        this.clients.values().forEach(EventProcessorClient::stop);
    }

    /**
     * Subscribe to an event hub in the context of a consumer group to consume events from all partitions.
     *
     * @param eventHubName the event hub name
     * @param consumerGroup the consumer group name
     * @param eventListener the listener {@link EventHubsEventMessageListener} to process Event Hub events.
     * @return the {@link EventProcessorClient} created to subscribe.
     */
    public EventProcessorClient subscribe(String eventHubName, String consumerGroup,
                                          EventHubsEventMessageListener eventListener, EventHubsEventListenerContainerSupport listenerContainerSupport) {
        EventProcessorClient processor = this.processorFactory.createProcessor(eventHubName, consumerGroup,
            eventListener, listenerContainerSupport);
        processor.start();

        this.clients.computeIfAbsent(new ConsumerIdentifier(eventHubName, consumerGroup), k -> processor);
        return processor;
    }

    /**
     * Unsubscribe to an event hub from a consumer group.
     *
     * @param eventHubName the event hub name
     * @param consumerGroup the consumer group name
     * @return true if unsubscribe successfully
     */
    public boolean unsubscribe(String eventHubName, String consumerGroup) {
        synchronized (this.clients) {
            EventProcessorClient processor = this.clients.remove(new ConsumerIdentifier(eventHubName, consumerGroup));
            if (processor == null) {
                LOGGER.warn("No EventProcessorClient for event hub {}, consumer group {}", eventHubName, consumerGroup);
                return false;
            }
            processor.stop();
            return true;
        }
    }

}
