package com.uber.api.customer.service.entity;

import com.uber.api.outbox.OutboxStatus;
import com.uber.api.saga.SagaStatus;
import lombok.*;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "driver_approval_outbox")
@Entity
public class DriverApprovalOutbox {

    @Id
    private UUID id;

    private UUID sagaId;

    private ZonedDateTime createdAt;

    private ZonedDateTime processedAt;

    private String type;

    @Column(length = 2500)
    private String payload;

    @Enumerated(EnumType.STRING)
    private SagaStatus sagaStatus;

    @Enumerated(EnumType.STRING)
    private OutboxStatus outboxStatus;

    @Version
    private int version;
}
