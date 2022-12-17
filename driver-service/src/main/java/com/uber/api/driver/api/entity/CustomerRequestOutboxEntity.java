package com.uber.api.driver.api.entity;


import com.uber.api.kafka.model.DriverStatus;
import com.uber.api.outbox.OutboxStatus;
import lombok.*;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer_request_outbox")
public class CustomerRequestOutboxEntity {

    @Id
    private UUID id;
    private UUID sagaId;
    private ZonedDateTime createdAt;
    private ZonedDateTime processedAt;
    private String type;
    @Column(length = 2500)
    private String payload;
    @Enumerated(EnumType.STRING)
    private OutboxStatus outboxStatus;
    private int version;

}
