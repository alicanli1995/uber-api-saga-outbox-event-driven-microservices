package com.uber.api.customer.service.dto;

import com.uber.api.common.api.constants.CustomerStatus;
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
public class TaxiPaymentOutboxMessage {
    private UUID id;

    private UUID sagaId;

    private ZonedDateTime createdAt;

    @Setter
    private ZonedDateTime processedAt;

    private String type;

    private String payload;

    @Setter
    private SagaStatus sagaStatus;

    @Setter
    private OutboxStatus outboxStatus;

    private int version;


}
