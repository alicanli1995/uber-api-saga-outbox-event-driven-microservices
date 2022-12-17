package com.uber.api.payment.service.event;

import com.uber.api.payment.service.dto.Payment;

import java.time.LocalDateTime;
import java.util.Collections;

public class PaymentCompletedEvent extends PaymentEvent {

    public PaymentCompletedEvent(Payment payment, LocalDateTime utc) {
        super(payment, utc, Collections.emptyList());
    }
}
