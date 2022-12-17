package com.uber.api.payment.service.event;

import com.uber.api.payment.service.dto.Payment;

import java.time.ZonedDateTime;
import java.util.Collections;

public class PaymentCompletedEvent extends PaymentEvent {

    public PaymentCompletedEvent(Payment payment, ZonedDateTime utc) {
        super(payment, utc, Collections.emptyList());
    }
}
