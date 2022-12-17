package com.uber.api.customer.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class DriverCallResponse {

    private String id;
    private String sagaId;
    private String driverMail;
    private String pendingRequestId;
    private Instant createdAt;
    private com.uber.api.kafka.model.DriverStatus driverStatus;
    private List<String> failureMessages;

}
