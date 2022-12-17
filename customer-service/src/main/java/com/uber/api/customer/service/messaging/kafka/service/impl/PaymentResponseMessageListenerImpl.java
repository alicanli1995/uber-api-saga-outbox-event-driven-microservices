package com.uber.api.customer.service.messaging.kafka.service.impl;

import com.uber.api.customer.service.dto.PaymentResponse;
import com.uber.api.customer.service.messaging.kafka.service.PaymentResponseMessageListener;
import com.uber.api.customer.service.saga.CustomerPaymentSaga;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class PaymentResponseMessageListenerImpl implements PaymentResponseMessageListener {

    private final CustomerPaymentSaga customerPaymentSaga;

    @Override
    public void paymentCompleted(PaymentResponse paymentResponse) {
        customerPaymentSaga.process(paymentResponse);
        log.info("Customer Payment saga process completed id {}", paymentResponse.getPaymentId());
    }

    @Override
    public void paymentCancelled(PaymentResponse paymentResponse) {
        customerPaymentSaga.rollback(paymentResponse);
        log.info("Payment cancelled for customer with mail: {}", paymentResponse.getCustomerMail());
    }
}
