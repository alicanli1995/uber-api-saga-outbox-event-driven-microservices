package com.uber.api.customer.service.messaging.kafka.listener;


import com.uber.api.common.api.constants.PaymentStatus;
import com.uber.api.common.api.constants.WebSocketDataType;
import com.uber.api.common.api.dto.CallTaxiEventPayload;
import com.uber.api.common.api.event.UberWebSocketEvent;
import com.uber.api.customer.service.event.CallRequestEvent;
import com.uber.api.customer.service.messaging.helper.CustomerMessagingDataMapper;
import com.uber.api.customer.service.messaging.kafka.service.PaymentResponseMessageListener;
import com.uber.api.kafka.consumer.KafkaConsumer;
import com.uber.api.kafka.model.PaymentResponseAvroModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResponseKafkaListener implements KafkaConsumer<PaymentResponseAvroModel> {

    private final PaymentResponseMessageListener paymentResponseMessageListener;
    private final CustomerMessagingDataMapper customerMessagingDataMapper;
    private final UberWebSocketEvent uberWebSocketEvent;

    @Override
    @KafkaListener(
            id = "${kafka-consumer-config.payment-consumer-group-id}",
            topics = "${customer-service.payment-response-topic-name}"
    )
    public void receive(@Payload List<PaymentResponseAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offSets) {
        log.info("{} number of payment responses received with keys:{}, partitions:{} and offsets: {}",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offSets.toString());

        messages.forEach(paymentResponseAvroModel -> {
            try {
                if (Objects.equals(PaymentStatus.COMPLETED.toString(), paymentResponseAvroModel.getPaymentStatus().toString())) {
                    log.info("Processing successful payment for customer id: {}", paymentResponseAvroModel.getRequestId());
                    paymentResponseMessageListener.paymentCompleted(customerMessagingDataMapper
                            .paymentResponseAvroModelToPaymentResponse(paymentResponseAvroModel));
                    CallTaxiEventPayload callTaxiEventPayload = customerMessagingDataMapper.createCallTaxiEventPayload(paymentResponseAvroModel);
                    uberWebSocketEvent.publishEvent(new CallRequestEvent(
                            callTaxiEventPayload,
                            callTaxiEventPayload.driverEmail(),
                            WebSocketDataType.CUSTOMER
                    ));
                } else if (Objects.equals(PaymentStatus.CANCELED.toString(), paymentResponseAvroModel.getPaymentStatus().toString()) ||
                        PaymentStatus.FAILED.toString().equals(paymentResponseAvroModel.getPaymentStatus().toString())) {
                    log.info("Processing unsuccessful payment for customer id: {}", paymentResponseAvroModel.getRequestId());
                    paymentResponseMessageListener.paymentCancelled(customerMessagingDataMapper
                            .paymentResponseAvroModelToPaymentResponse(paymentResponseAvroModel));
                }
            } catch (OptimisticLockingFailureException e) {
                //NO-OP for optimistic lock. This means another thread finished the work, do not throw error to prevent reading the data from kafka again!
                log.error("Caught optimistic locking exception in PaymentResponseKafkaListener for request id: {}",
                        paymentResponseAvroModel.getRequestId());
            }
        });

    }
}
