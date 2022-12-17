package com.uber.api.payment.service.dto;

import com.uber.api.common.api.constants.PaymentStatus;
import com.uber.api.outbox.OutboxStatus;
import com.uber.api.saga.SagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class CustomerOutboxMessage {
    private UUID id;
    private UUID sagaId;
    private ZonedDateTime createdAt;
    private ZonedDateTime processedAt;
    private String type;
    private String payload;
    private SagaStatus sagaStatus;
    private PaymentStatus paymentStatus;
    @Setter
    private OutboxStatus outboxStatus;
    private int version;
}
