package com.uber.api.payment.service.helper;

import com.uber.api.payment.service.dto.CallEventPayload;
import com.uber.api.payment.service.event.PaymentEvent;
import org.springframework.stereotype.Component;

@Component
public class PaymentDataMapper {

    public CallEventPayload paymentEventToCustomerEventPayload(PaymentEvent paymentEvent) {
        return CallEventPayload.builder()
                .paymentId(paymentEvent.getPayment().getId().toString())
                .requestId(paymentEvent.getPayment().getRequestId())
                .price(paymentEvent.getPayment().getPrice())
                .createdAt(paymentEvent.getCreatedAt())
                .failureMessages(paymentEvent.getFailureMessages())
                .customerMail(paymentEvent.getPayment().getCustomerMail())
                .paymentStatus(paymentEvent.getPayment().getStatus().toString())
                .build();
    }
}
