package com.uber.api.payment.service.messaging.service;

import com.uber.api.payment.service.dto.PaymentRequest;

public interface PaymentRequestMessageListener {
    void completePayment(PaymentRequest paymentRequestAvroModelToPaymentRequest);

    void cancelPayment(PaymentRequest paymentRequestAvroModelToPaymentRequest);

}
