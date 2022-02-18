// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.servicebus.core.listener;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.messaging.ConsumerIdentifier;
import com.azure.spring.messaging.container.AbstractMessageListenerContainer;
import com.azure.spring.service.servicebus.processor.ServiceBusMessageListenerContainerSupport;
import com.azure.spring.service.servicebus.processor.ServiceBusRecordMessageListener;
import com.azure.spring.servicebus.core.processor.ServiceBusProcessorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.Lifecycle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A processor container using {@link ServiceBusProcessorClient} to subscribe to Service Bus queue/topic entities and
 * consumer messages.
 * <p>
 * For different combinations of Service Bus entity name and subscription, different {@link ServiceBusProcessorClient}s
 * will be created to subscribe to it.
 * </p>
 * <p>
 * Implementation of {@link AbstractMessageListenerContainer} is required when using {@link ServiceBusProcessorClient}
 * to consume messages.
 *
 * @see AbstractMessageListenerContainer
 */
public class ServiceBusMessageListenerContainer extends AbstractMessageListenerContainer implements Lifecycle,
    DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusMessageListenerContainer.class);

    private final ServiceBusProcessorFactory processorFactory;
    private final Map<ConsumerIdentifier, ServiceBusProcessorClient> clients = new HashMap<>();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final List<ServiceBusProcessorFactory.Listener> listeners = new ArrayList<>();

    /**
     * Create an instance using the supplied processor factory.
     *
     * @param processorFactory the processor factory.
     */
    public ServiceBusMessageListenerContainer(ServiceBusProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }

    public ServiceBusProcessorFactory getProcessorFactory() {
        return processorFactory;
    }

    @Override
    public void destroy() {
        this.clients.values().forEach(ServiceBusProcessorClient::close);
        this.clients.clear();
    }

    @Override
    protected void doStart() {
        this.clients.values().forEach(ServiceBusProcessorClient::start);
    }

    @Override
    protected void doStop() {
        this.clients.values().forEach(ServiceBusProcessorClient::stop);
    }

    @Override
    public boolean isRunning() {
        return this.isRunning.get();
    }

    /**
     * Subscribe to a queue to consumer messages.
     *
     * @param queue the queue
     * @param listener the listener to process messages.
     * @return the {@link ServiceBusProcessorClient} created to subscribe to the queue.
     */
    public ServiceBusProcessorClient subscribe(String queue, ServiceBusRecordMessageListener listener, ServiceBusMessageListenerContainerSupport listenerContainerSupport) {
        ServiceBusProcessorClient processor = this.processorFactory.createProcessor(queue, listener, listenerContainerSupport);
        processor.start();
        this.listeners.forEach(l -> l.processorAdded(queue, null, processor));
        this.clients.computeIfAbsent(new ConsumerIdentifier(queue), k -> processor);
        return processor;
    }

    /**
     * Unsubscribe to a queue.
     *
     * @param queue the queue.
     * @return true if unsubscribe successfully.
     */
    public boolean unsubscribe(String queue) {
        synchronized (this.clients) {
            ServiceBusProcessorClient processor = this.clients.remove(new ConsumerIdentifier(queue));
            if (processor == null) {
                LOGGER.warn("No ServiceBusProcessorClient for Service Bus queue {}", queue);
                return false;
            }
            processor.close();
            this.listeners.forEach(l -> l.processorRemoved(queue, null, processor));
            return true;
        }
    }

    /**
     * Subscribe to a topic in the context of a subscription to consumer messages.
     *
     * @param topic the topic.
     * @param subscription the subscription.
     * @param listener the listener to process messages.
     * @return the {@link ServiceBusProcessorClient} created to subscribe to the topic.
     */
    public ServiceBusProcessorClient subscribe(String topic, String subscription, ServiceBusRecordMessageListener listener, ServiceBusMessageListenerContainerSupport listenerContainerSupport) {
        ServiceBusProcessorClient processor = this.processorFactory.createProcessor(topic, subscription, listener, listenerContainerSupport);
        processor.start();
        this.listeners.forEach(l -> l.processorAdded(topic, subscription, processor));
        this.clients.computeIfAbsent(new ConsumerIdentifier(topic, subscription), k -> processor);
        return processor;
    }

    /**
     * Unsubscribe to a topic from a subscription.
     *
     * @param topic the topic.
     * @param subscription the subscription.
     * @return true if unsubscribe successfully.
     */
    public boolean unsubscribe(String topic, String subscription) {
        synchronized (this.clients) {
            ServiceBusProcessorClient processor = this.clients.remove(new ConsumerIdentifier(topic, subscription));
            if (processor == null) {
                LOGGER.warn("No ServiceBusProcessorClient for Service Bus topic {}, subscription {}",
                    topic, subscription);
                return false;
            }
            processor.close();
            this.listeners.forEach(l -> l.processorRemoved(topic, subscription, processor));
            return true;
        }
    }

}
