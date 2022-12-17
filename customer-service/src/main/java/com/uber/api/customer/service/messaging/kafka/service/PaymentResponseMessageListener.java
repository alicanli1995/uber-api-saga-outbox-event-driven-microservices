package com.uber.api.customer.service.messaging.kafka.service;

import com.uber.api.customer.service.dto.PaymentResponse;

public interface PaymentResponseMessageListener {

    void paymentCompleted(PaymentResponse paymentResponse);

    void paymentCancelled(PaymentResponse paymentResponse);

}
