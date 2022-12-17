package com.uber.api.customer.service.dto;

import com.uber.api.kafka.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PaymentResponse {

    private String id;
    private String sagaId;
    private String requestId;
    private String paymentId;
    private String customerMail;
    private BigDecimal price;
    private Instant createdAt;
    private PaymentStatus paymentStatus;
    private List<String> failureMessages;
}
