package com.azure.spring.eventhubs.core.listener.adapter;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.spring.eventhubs.checkpoint.EventCheckpointManager;
import com.azure.spring.eventhubs.support.EventHubsHeaders;
import com.azure.spring.eventhubs.support.converter.EventHubsBatchMessageConverter;
import com.azure.spring.messaging.AzureHeaders;
import com.azure.spring.messaging.checkpoint.AzureCheckpointer;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.messaging.checkpoint.Checkpointer;
import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.service.eventhubs.processor.EventHubsBatchEventMessageListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Adapter for batch event listener.
 */
public class BatchEventListenerAdapter implements EventHubsBatchEventMessageListener {

    private final Consumer<Message<?>> consumer;

    private final CheckpointConfig checkpointConfig;

    private final EventCheckpointManager checkpointManager;

    private AzureMessageConverter<EventBatchContext, EventData> messageConverter = new EventHubsBatchMessageConverter();

    private Class<?> payloadType = byte[].class;

    public BatchEventListenerAdapter(Consumer<Message<?>> consumer, CheckpointConfig checkpointConfig,
                                     EventCheckpointManager checkpointManager) {
        this.consumer = consumer;
        this.checkpointConfig = checkpointConfig;
        this.checkpointManager = checkpointManager;
    }

    @Override
    public void onEventBatch(EventBatchContext eventBatchContext) {
        PartitionContext partition = eventBatchContext.getPartitionContext();

        Map<String, Object> headers = new HashMap<>();
        headers.put(AzureHeaders.RAW_PARTITION_ID, partition.getPartitionId());
        headers.put(EventHubsHeaders.LAST_ENQUEUED_EVENT_PROPERTIES,
            eventBatchContext.getLastEnqueuedEventProperties());

        Checkpointer checkpointer = new AzureCheckpointer(eventBatchContext::updateCheckpointAsync);
        if (CheckpointMode.MANUAL.equals(checkpointConfig.getMode())) {
            headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
        }

        Message<?> message = this.messageConverter.toMessage(eventBatchContext, new MessageHeaders(headers),
            payloadType);

        consumer.accept(message);
        if (checkpointConfig.getMode().equals(CheckpointMode.BATCH)) {
            checkpointManager.checkpoint(eventBatchContext);
        }
    }

    /**
     * Set message converter.
     *
     * @param converter the converter
     */
    public void setMessageConverter(AzureMessageConverter<EventBatchContext, EventData> converter) {
        this.messageConverter = converter;
    }

    /**
     * Set payload type.
     *
     * @param payloadType the payload type
     */
    public void setPayloadType(Class<?> payloadType) {
        this.payloadType = payloadType;
    }

}
