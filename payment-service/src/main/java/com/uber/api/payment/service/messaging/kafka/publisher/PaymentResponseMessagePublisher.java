package com.uber.api.payment.service.messaging.kafka.publisher;

import com.uber.api.outbox.OutboxStatus;
import com.uber.api.payment.service.dto.CustomerOutboxMessage;

import java.util.function.BiConsumer;

public interface PaymentResponseMessagePublisher {
    void publish(CustomerOutboxMessage message,
                 BiConsumer<CustomerOutboxMessage , OutboxStatus> updateOutboxMessage);
}
