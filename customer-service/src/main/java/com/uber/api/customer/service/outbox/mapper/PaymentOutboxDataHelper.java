package com.uber.api.customer.service.outbox.mapper;

import com.uber.api.customer.service.entity.BalanceOutboxEntity;
import com.uber.api.customer.service.dto.TaxiPaymentOutboxMessage;
import org.springframework.stereotype.Component;

@Component
public class PaymentOutboxDataHelper {
    public TaxiPaymentOutboxMessage paymentOutboxEntityToCustomerPaymentOutboxMessage(BalanceOutboxEntity balanceOutboxEntity) {
        return TaxiPaymentOutboxMessage.builder()
                .id(balanceOutboxEntity.getId())
                .sagaId(balanceOutboxEntity.getSagaId())
                .createdAt(balanceOutboxEntity.getCreatedAt())
                .processedAt(balanceOutboxEntity.getProcessedAt())
                .type(balanceOutboxEntity.getType())
                .payload(balanceOutboxEntity.getPayload())
                .sagaStatus(balanceOutboxEntity.getSagaStatus())
                .outboxStatus(balanceOutboxEntity.getOutboxStatus())
                .version(balanceOutboxEntity.getVersion())
                .build();

    }

    public BalanceOutboxEntity taxiPaymentOutboxMessageToPaymentOutboxEntity(TaxiPaymentOutboxMessage taxiPaymentOutboxMessage) {

        return BalanceOutboxEntity.builder()
                .id(taxiPaymentOutboxMessage.getId())
                .sagaId(taxiPaymentOutboxMessage.getSagaId())
                .createdAt(taxiPaymentOutboxMessage.getCreatedAt())
                .processedAt(taxiPaymentOutboxMessage.getProcessedAt())
                .type(taxiPaymentOutboxMessage.getType())
                .payload(taxiPaymentOutboxMessage.getPayload())
                .sagaStatus(taxiPaymentOutboxMessage.getSagaStatus())
                .outboxStatus(taxiPaymentOutboxMessage.getOutboxStatus())
                .version(taxiPaymentOutboxMessage.getVersion())
                .build();

    }
}
