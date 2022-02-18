// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhubs.inbound;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.spring.eventhubs.checkpoint.CheckpointManagers;
import com.azure.spring.eventhubs.checkpoint.EventCheckpointManager;
import com.azure.spring.eventhubs.core.listener.EventHubsMessageListenerContainer;
import com.azure.spring.eventhubs.core.listener.adapter.BatchEventListenerAdapter;
import com.azure.spring.eventhubs.core.listener.adapter.RecordEventListenerAdapter;
import com.azure.spring.integration.eventhubs.inbound.health.EventHubsProcessorInstrumentation;
import com.azure.spring.integration.instrumentation.Instrumentation;
import com.azure.spring.integration.instrumentation.InstrumentationManager;
import com.azure.spring.messaging.ListenerMode;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.service.eventhubs.processor.EventHubsEventListenerContainerSupport;
import com.azure.spring.service.eventhubs.processor.consumer.EventHubsCloseContextConsumer;
import com.azure.spring.service.eventhubs.processor.consumer.EventHubsErrorContextConsumer;
import com.azure.spring.service.eventhubs.processor.consumer.EventHubsInitializationContextConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import java.util.function.Consumer;

/**
 * Message driven inbound channel adapter for Azure Event Hubs.
 * <p>
 * Example:
 * <pre> <code>
 *   {@literal @}ServiceActivator(inputChannel = "input")
 *     public void messageReceiver(byte[] payload, @Header(AzureHeaders.CHECKPOINTER) Checkpointer checkpointer) {
 *         String message = new String(payload);
 *         LOGGER.info("New message received: '{}'", message);
 *         checkpointer.success()
 *                 .doOnSuccess(s -&gt; LOGGER.info("Message '{}' successfully checkpointed", message))
 *                 .doOnError(e -&gt; LOGGER.error("Error found", e))
 *                 .subscribe();
 *     }
 *
 *    {@literal @}Bean
 *     public EventHubsInboundChannelAdapter messageChannelAdapter(
 *             {@literal @}Qualifier("input") MessageChannel inputChannel,
 *             EventHubsProcessorContainer processorContainer) {
 *         CheckpointConfig config = new CheckpointConfig(CheckpointMode.MANUAL);
 *
 *         EventHubsInboundChannelAdapter adapter =
 *                 new EventHubsInboundChannelAdapter(processorContainer, "eventhub-name",
 *                         "consumer-group-name", config);
 *         adapter.setOutputChannel(inputChannel);
 *         return adapter;
 *     }
 *
 *    {@literal @}Bean
 *     public MessageChannel input() {
 *         return new DirectChannel();
 *     }
 * </code> </pre>
 */
public class EventHubsInboundChannelAdapter extends MessageProducerSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubsInboundChannelAdapter.class);
    private final String eventHubName;
    private final String consumerGroup;

    private final EventHubsMessageListenerContainer processorContainer;
    private IntegrationRecordEventListener recordListener;
    private IntegrationBatchEventListener batchListener;
    private final ListenerMode listenerMode;

    private final CheckpointConfig checkpointConfig;
    private Class<?> payloadType;

    /**
     * Construct a {@link EventHubsInboundChannelAdapter} with the specified {@link EventHubsMessageListenerContainer},
     * event Hub Name , consumer Group and {@link CheckpointConfig}.
     *
     * @param processorContainer the processor container
     * @param eventHubName the eventHub name
     * @param consumerGroup the consumer group
     * @param checkpointConfig the checkpoint config
     */
    public EventHubsInboundChannelAdapter(EventHubsMessageListenerContainer processorContainer,
                                          String eventHubName, String consumerGroup,
                                          CheckpointConfig checkpointConfig) {
        this(processorContainer, eventHubName, consumerGroup, ListenerMode.RECORD, checkpointConfig);
    }

    /**
     * Construct a {@link EventHubsInboundChannelAdapter} with the specified {@link EventHubsMessageListenerContainer},
     * event Hub Name , consumer Group, {@link ListenerMode} and {@link CheckpointConfig}.
     *
     * @param eventProcessorsContainer the event processors container
     * @param eventHubName the eventHub name
     * @param consumerGroup the consumer group
     * @param listenerMode the listener mode
     * @param checkpointConfig the checkpoint config
     */
    public EventHubsInboundChannelAdapter(EventHubsMessageListenerContainer eventProcessorsContainer,
                                          String eventHubName, String consumerGroup,
                                          ListenerMode listenerMode,
                                          CheckpointConfig checkpointConfig) {
        Assert.notNull(eventHubName, "eventhubName must be provided");
        Assert.notNull(consumerGroup, "consumerGroup must be provided");

        this.processorContainer = eventProcessorsContainer;
        this.eventHubName = eventHubName;
        this.consumerGroup = consumerGroup;
        this.listenerMode = listenerMode;
        this.checkpointConfig = checkpointConfig;
    }

    @Override
    protected void onInit() {
        super.onInit();

        EventCheckpointManager checkpointManager = CheckpointManagers.of(checkpointConfig, this.listenerMode);

        this.recordListener = new IntegrationRecordEventListener(super::sendMessage, checkpointConfig
            , checkpointManager);
        this.batchListener = new IntegrationBatchEventListener(super::sendMessage, checkpointConfig,
            checkpointManager);

        if (ListenerMode.BATCH.equals(this.listenerMode)) {
            this.processorContainer.subscribe(this.eventHubName, this.consumerGroup, this.batchListener, this.batchListener);
            if (this.payloadType != null) {
                this.batchListener.setPayloadType(payloadType);
            }
        } else {
            this.processorContainer.subscribe(this.eventHubName, this.consumerGroup, this.recordListener, this.recordListener);
            if (this.payloadType != null) {
                this.recordListener.setPayloadType(payloadType);
            }
        }
    }

    @Override
    public void doStart() {
        this.processorContainer.start();
    }

    @Override
    protected void doStop() {
        this.processorContainer.stop();
    }

    /**
     * Set message converter.
     *
     * @param messageConverter the message converter
     */
    public void setMessageConverter(AzureMessageConverter<EventData, EventData> messageConverter) {
        this.recordListener.setMessageConverter(messageConverter);
    }

    /**
     * Set message converter.
     *
     * @param messageConverter the message converter
     */
    public void setBatchMessageConverter(AzureMessageConverter<EventBatchContext, EventData> messageConverter) {
        this.batchListener.setMessageConverter(messageConverter);
    }

    /**
     * Set payload Type.
     *
     * @param payloadType the payload Type
     */
    public void setPayloadType(Class<?> payloadType) {
        this.payloadType = payloadType;
    }

    /**
     * Set instrumentation Manager.
     *
     * @param instrumentationManager the instrumentation Manager
     */
    public void setInstrumentationManager(InstrumentationManager instrumentationManager) {
        if (ListenerMode.BATCH.equals(this.listenerMode)) {
            this.batchListener.setInstrumentationManager(instrumentationManager);
        } else {
            this.recordListener.setInstrumentationManager(instrumentationManager);
        }
    }

    /**
     * Set instrumentation id.
     *
     * @param instrumentationId the instrumentation id
     */
    public void setInstrumentationId(String instrumentationId) {
        if (ListenerMode.BATCH.equals(this.listenerMode)) {
            this.batchListener.setInstrumentationId(instrumentationId);
        } else {
            this.recordListener.setInstrumentationId(instrumentationId);
        }
    }

    /**
     * Instrumentation aware interface.
     */
    private interface InstrumentationAware {

        default void updateInstrumentation(ErrorContext errorContext,
                                           InstrumentationManager instrumentationManager,
                                           String instrumentationId) {
            if (instrumentationManager == null) {
                return;
            }

            Instrumentation instrumentation = instrumentationManager.getHealthInstrumentation(instrumentationId);
            if (instrumentation != null) {
                if (instrumentation instanceof EventHubsProcessorInstrumentation) {
                    ((EventHubsProcessorInstrumentation) instrumentation).markError(errorContext);
                } else {
                    instrumentation.markDown(errorContext.getThrowable());
                }
            }
        }
    }

    private static class IntegrationRecordEventListener extends RecordEventListenerAdapter implements InstrumentationAware, EventHubsEventListenerContainerSupport {

        private InstrumentationManager instrumentationManager;
        private String instrumentationId;

        public IntegrationRecordEventListener(Consumer<Message<?>> consumer,
                                              CheckpointConfig checkpointConfig,
                                              EventCheckpointManager checkpointManager) {
            super(consumer, checkpointConfig, checkpointManager);
        }

        @Override
        public EventHubsErrorContextConsumer getErrorContextConsumer() {
            return errorContext -> {
                LOGGER.error("Record event error occurred on partition: {}. Error: {}",
                    errorContext.getPartitionContext().getPartitionId(),
                    errorContext.getThrowable());
                updateInstrumentation(errorContext, instrumentationManager, instrumentationId);
            };
        }

        @Override
        public EventHubsCloseContextConsumer getCloseContextConsumer() {
            return closeContext -> LOGGER.info("Stopped receiving on partition: {}. Reason: {}",
                closeContext.getPartitionContext().getPartitionId(),
                closeContext.getCloseReason());
        }

        @Override
        public EventHubsInitializationContextConsumer getInitializationContextConsumer() {
            return initializationContext -> LOGGER.info("Started receiving on partition: {}",
                initializationContext.getPartitionContext().getPartitionId());
        }

        public void setInstrumentationManager(InstrumentationManager instrumentationManager) {
            this.instrumentationManager = instrumentationManager;
        }

        public void setInstrumentationId(String instrumentationId) {
            this.instrumentationId = instrumentationId;
        }
    }

    private static class IntegrationBatchEventListener extends BatchEventListenerAdapter implements InstrumentationAware, EventHubsEventListenerContainerSupport {

        private InstrumentationManager instrumentationManager;
        private String instrumentationId;

        public IntegrationBatchEventListener(Consumer<Message<?>> consumer,
                                             CheckpointConfig checkpointConfig,
                                             EventCheckpointManager checkpointManager) {
            super(consumer, checkpointConfig, checkpointManager);
        }

        @Override
        public EventHubsErrorContextConsumer getErrorContextConsumer() {
            return errorContext -> {
                LOGGER.error("Error occurred on partition: {}. Error: {}",
                    errorContext.getPartitionContext().getPartitionId(),
                    errorContext.getThrowable());
                updateInstrumentation(errorContext, instrumentationManager, instrumentationId);
            };
        }

        @Override
        public EventHubsCloseContextConsumer getCloseContextConsumer() {
            return closeContext -> LOGGER.info("Stopped receiving on partition: {}. Reason: {}",
                closeContext.getPartitionContext().getPartitionId(),
                closeContext.getCloseReason());
        }

        @Override
        public EventHubsInitializationContextConsumer getInitializationContextConsumer() {
            return initializationContext -> LOGGER.info("Started receiving on partition: {}",
                initializationContext.getPartitionContext().getPartitionId());
        }

        public void setInstrumentationManager(InstrumentationManager instrumentationManager) {
            this.instrumentationManager = instrumentationManager;
        }

        public void setInstrumentationId(String instrumentationId) {
            this.instrumentationId = instrumentationId;
        }

    }

}
