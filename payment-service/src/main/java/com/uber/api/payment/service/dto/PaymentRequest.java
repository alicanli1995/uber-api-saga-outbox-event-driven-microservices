package com.uber.api.payment.service.dto;

import com.uber.api.kafka.model.PaymentCustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class PaymentRequest {
    private String id;
    private String sagaId;
    private String requestId;
    private String customerMail;
    private BigDecimal price;
    private Instant createdAt;
    private PaymentCustomerStatus status;

    public void setStatus(PaymentCustomerStatus status) {
        this.status = status;
    }
}
