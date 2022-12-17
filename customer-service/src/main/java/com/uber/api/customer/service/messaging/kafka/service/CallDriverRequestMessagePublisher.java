package com.uber.api.customer.service.messaging.kafka.service;

import com.uber.api.customer.service.entity.DriverApprovalOutbox;
import com.uber.api.outbox.OutboxStatus;

import java.util.function.BiConsumer;

public interface CallDriverRequestMessagePublisher {
    void publish(DriverApprovalOutbox callOutboxMessage, BiConsumer<DriverApprovalOutbox, OutboxStatus> outboxCallback);
}
