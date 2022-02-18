//package com.azure.spring.servicebus.core;
//
//
//import com.azure.messaging.servicebus.ServiceBusMessage;
//import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
//import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
//import com.azure.spring.messaging.checkpoint.AzureCheckpointer;
//import com.azure.spring.messaging.checkpoint.CheckpointConfig;
//import com.azure.spring.messaging.checkpoint.Checkpointer;
//import com.azure.spring.messaging.container.AbstractMessageListenerContainer;
//import com.azure.spring.messaging.converter.AzureMessageConverter;
//import com.azure.spring.service.servicebus.processor.RecordMessageProcessingListener;
//import com.azure.spring.service.servicebus.processor.consumer.ServiceBusErrorContextConsumer;
//import com.azure.spring.servicebus.support.ServiceBusMessageHeaders;
//import com.azure.spring.servicebus.support.converter.ServiceBusMessageConverter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageHeaders;
//import reactor.core.publisher.Mono;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class ServiceBusMessageListenerContainer extends AbstractMessageListenerContainer {
//
//    private final ServiceBusProcessorContainer serviceBusProcessorContainer;
//    private final CheckpointConfig checkpointConfig;
//    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusMessageListenerContainer.class);
//
//    public ServiceBusMessageListenerContainer(ServiceBusProcessorContainer serviceBusProcessorContainer,
//                                              CheckpointConfig checkpointConfig) {
//        this.serviceBusProcessorContainer = serviceBusProcessorContainer;
//        this.checkpointConfig = checkpointConfig;
//    }
//
//    @Override
//    protected void doStart() {
//        synchronized (this.getLifecycleMonitor()) {
//            serviceBusProcessorContainer.subscribe(getDestination(), getGroup(), new RecordMessageProcessingListener() {
//
//                    private AzureMessageConverter<ServiceBusReceivedMessage, ServiceBusMessage> messageConverter =
//                        new ServiceBusMessageConverter();
//                    private final Class<?> payloadType = byte[].class;
//
//                    private static final String MSG_FAIL_CHECKPOINT = "Failed to checkpoint %s";
//                    private static final String MSG_SUCCESS_CHECKPOINT = "Checkpointed %s in %s mode";
//
//                    @Override
//                    public void onMessage(ServiceBusReceivedMessageContext messageContext) {
//                        Checkpointer checkpointer =
//                            new AzureCheckpointer(() -> Mono.fromRunnable(messageContext::complete),
//                                () -> Mono.fromRunnable(messageContext::abandon));
//                        Map<String, Object> headers = new HashMap<>();
//                        headers.put(ServiceBusMessageHeaders.RECEIVED_MESSAGE_CONTEXT, messageContext);
//
//                        Message<?> message = messageConverter.toMessage(messageContext.getMessage(),
//                            new MessageHeaders(headers),
//                            payloadType);
//
//                        if (null == message) {
//                            return;
//                        }
//                        getMessageHandler().handleMessage(message);
//
//                        checkpointer.success()
//                                    .doOnSuccess(t ->
//                                        LOGGER.debug(String.format(MSG_SUCCESS_CHECKPOINT, message,
//                                            checkpointConfig.getMode())))
//                                    .doOnError(t ->
//                                        LOGGER.warn(String.format(MSG_FAIL_CHECKPOINT, message), t))
//                                    .subscribe();
//
//                    }
//
//                    @Override
//                    public ServiceBusErrorContextConsumer getErrorContextConsumer() {
//                        return RecordMessageProcessingListener.super.getErrorContextConsumer();
//                    }
//
//                }
//            );
//
//        }
//    }
//
//    @Override
//    protected void doStop() {
//        synchronized (this.getLifecycleMonitor()) {
//            serviceBusProcessorContainer.unsubscribe(getDestination(), getGroup());
//        }
//    }
//}
