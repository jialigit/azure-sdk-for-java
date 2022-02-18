// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.container;

import com.azure.spring.messaging.listener.AzureMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;

/**
 * The base implementation for the {@link MessageListenerContainer}.
 */
public abstract class AbstractMessageListenerContainer implements MessageListenerContainer, BeanNameAware,
    DisposableBean {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractMessageListenerContainer.class);

    private final Object lifecycleMonitor = new Object();

    private String destination;

    private String group;


    private AzureMessageHandler messageHandler;

    //Settings that are changed at runtime
    private boolean active;

    private volatile boolean running = false;

    private String beanName;

    protected abstract void doStart();

    protected abstract void doStop();

    protected void doDestroy() {
    }

    @Override
    public void stop(Runnable callback) {
        this.stop();
        callback.run();
    }

    @Override
    public void start() {
        LOG.debug("Starting container with name {}", getBeanName());
        synchronized (this.getLifecycleMonitor()) {
            this.running = true;
            this.getLifecycleMonitor().notifyAll();
        }
        doStart();
        this.active = true;
    }

    @Override
    public void stop() {
        LOG.debug("Stopping container with name {}", getBeanName());

        synchronized (this.getLifecycleMonitor()) {
            this.running = false;
            this.getLifecycleMonitor().notifyAll();
        }
        doStop();
    }

    @Override
    public boolean isRunning() {
        synchronized (this.getLifecycleMonitor()) {
            return this.running;
        }
    }

    @Override
    public void destroy() throws Exception {
        synchronized (this.lifecycleMonitor) {
            stop();
            this.active = false;
            doDestroy();
        }
    }

    public String getBeanName() {
        return beanName;
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Object getLifecycleMonitor() {
        return lifecycleMonitor;
    }

    @Override
    public int getPhase() {
        return 0;
    }

    @Override
    public AzureMessageHandler getMessageHandler() {
        return messageHandler;
    }

    @Override
    public void setMessageHandler(AzureMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDestination() {
        return destination;
    }

    public String getGroup() {
        return group;
    }

    public boolean isActive() {
        return active;
    }
}
