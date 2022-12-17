package com.uber.api.payment.service.event;

import com.uber.api.common.api.event.UberDomainEvent;
import com.uber.api.payment.service.dto.Payment;

import java.time.LocalDateTime;
import java.util.List;

public abstract class PaymentEvent implements UberDomainEvent<Payment> {
    private final Payment payment;
    private final LocalDateTime createdAt;
    private final List<String> failureMessages;

    public PaymentEvent(Payment payment, LocalDateTime createdAt, List<String> failureMessages) {
        this.payment = payment;
        this.createdAt = createdAt;
        this.failureMessages = failureMessages;
    }

    public Payment getPayment() {
        return payment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }
}
