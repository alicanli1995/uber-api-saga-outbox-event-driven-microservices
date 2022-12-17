package com.uber.api.payment.service.outbox.helper;

import com.uber.api.payment.service.dto.CustomerOutboxMessage;
import com.uber.api.payment.service.entity.PaymentOutboxEntity;
import org.springframework.stereotype.Component;

@Component
public class OutboxDataHelper {
    public PaymentOutboxEntity toCustomerOutboxEntity(CustomerOutboxMessage customerOutboxMessage) {
        return PaymentOutboxEntity.builder()
                .id(customerOutboxMessage.getId())
                .sagaId(customerOutboxMessage.getSagaId())
                .createdAt(customerOutboxMessage.getCreatedAt())
                .type(customerOutboxMessage.getType())
                .payload(customerOutboxMessage.getPayload())
                .outboxStatus(customerOutboxMessage.getOutboxStatus())
                .paymentStatus(customerOutboxMessage.getPaymentStatus())
                .version(customerOutboxMessage.getVersion())
                .build();
    }

    public CustomerOutboxMessage toCustomerOutboxMessage(PaymentOutboxEntity paymentOutboxEntity) {
        return CustomerOutboxMessage.builder()
                .id(paymentOutboxEntity.getId())
                .sagaId(paymentOutboxEntity.getSagaId())
                .createdAt(paymentOutboxEntity.getCreatedAt())
                .type(paymentOutboxEntity.getType())
                .payload(paymentOutboxEntity.getPayload())
                .outboxStatus(paymentOutboxEntity.getOutboxStatus())
                .paymentStatus(paymentOutboxEntity.getPaymentStatus())
                .version(paymentOutboxEntity.getVersion())
                .build();
    }
}
