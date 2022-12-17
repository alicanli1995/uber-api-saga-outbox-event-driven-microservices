package com.uber.api.customer.service.messaging.kafka.service;

import com.uber.api.customer.service.dto.TaxiPaymentOutboxMessage;
import com.uber.api.outbox.OutboxStatus;

import java.util.function.BiConsumer;

public interface PaymentRequestMessagePublisher {

    void publish(TaxiPaymentOutboxMessage message, BiConsumer<TaxiPaymentOutboxMessage, OutboxStatus> callback);

}
