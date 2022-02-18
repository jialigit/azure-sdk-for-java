package com.azure.spring.eventhubs.core.listener.adapter;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.spring.eventhubs.checkpoint.EventCheckpointManager;
import com.azure.spring.eventhubs.support.EventHubsHeaders;
import com.azure.spring.eventhubs.support.converter.EventHubsMessageConverter;
import com.azure.spring.messaging.AzureHeaders;
import com.azure.spring.messaging.checkpoint.AzureCheckpointer;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.messaging.checkpoint.Checkpointer;
import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.service.eventhubs.processor.EventHubsRecordEventMessageListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Adapter for record event listener.
 */
public class RecordEventListenerAdapter implements EventHubsRecordEventMessageListener {

    private final Consumer<Message<?>> consumer;

    private final CheckpointConfig checkpointConfig;

    private final EventCheckpointManager checkpointManager;

    private AzureMessageConverter<EventData, EventData> messageConverter = new EventHubsMessageConverter();

    private Class<?> payloadType = byte[].class;

    public RecordEventListenerAdapter(Consumer<Message<?>> consumer, CheckpointConfig checkpointConfig,
                                      EventCheckpointManager checkpointManager) {
        this.consumer = consumer;
        this.checkpointConfig = checkpointConfig;
        this.checkpointManager = checkpointManager;
    }

    @Override
    public void onEvent(EventContext eventContext) {
        PartitionContext partition = eventContext.getPartitionContext();

        Map<String, Object> headers = new HashMap<>();
        headers.put(AzureHeaders.RAW_PARTITION_ID, partition.getPartitionId());
        headers.put(EventHubsHeaders.LAST_ENQUEUED_EVENT_PROPERTIES, eventContext.getLastEnqueuedEventProperties());

        final EventData event = eventContext.getEventData();

        Checkpointer checkpointer = new AzureCheckpointer(eventContext::updateCheckpointAsync);
        if (CheckpointMode.MANUAL.equals(checkpointConfig.getMode())) {
            headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
        }

        Message<?> message = this.messageConverter.toMessage(event, new MessageHeaders(headers), payloadType);

        consumer.accept(message);
        checkpointManager.checkpoint(eventContext);
    }

    /**
     * Set message converter.
     *
     * @param converter the converter
     */
    public void setMessageConverter(AzureMessageConverter<EventData, EventData> converter) {
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
