package com.uber.api.payment.service.messaging.service;

import com.uber.api.payment.service.entity.Balance;
import com.uber.api.payment.service.dto.Payment;
import com.uber.api.payment.service.event.PaymentEvent;

import java.util.List;

public interface PaymentService {
    PaymentEvent validateAndInitializePayment(Payment payment, Balance creditEntry, List<String> failureMessage);

    PaymentEvent validateAndCancelPayment(Payment payment, Balance creditEntry, List<String> failureMessage);
}
