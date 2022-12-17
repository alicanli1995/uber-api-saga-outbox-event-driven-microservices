package com.uber.api.payment.service.event;

import com.uber.api.payment.service.dto.Payment;

import java.time.LocalDateTime;
import java.util.List;

public class PaymentFailedEvent extends PaymentEvent {
    public PaymentFailedEvent(Payment payment, LocalDateTime utc, List<String> failureMessage) {
        super(
                payment,
                utc,
                failureMessage
        );
    }
}
