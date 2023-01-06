package com.uber.api.payment.service.outbox.helper;

import com.uber.api.common.api.constants.PaymentStatus;
import com.uber.api.outbox.OutboxStatus;
import com.uber.api.payment.service.dto.CustomerOutboxMessage;
import com.uber.api.payment.service.entity.PaymentOutboxEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
class OutboxDataHelperTest {

    @Test
    void test_toCustomerOutboxEntity_shouldReturnCustomerOutboxEntity() {

        var customerOutboxMessage = CustomerOutboxMessage.builder()
                .id(UUID.randomUUID())
                .sagaId(UUID.randomUUID())
                .createdAt(ZonedDateTime.now())
                .type("type")
                .payload("payload")
                .outboxStatus(OutboxStatus.STARTED)
                .paymentStatus(PaymentStatus.COMPLETED)
                .version(1)
                .build();

        var outboxDataHelper = new OutboxDataHelper();

        var paymentOutboxEntity = outboxDataHelper.toCustomerOutboxEntity(customerOutboxMessage);

        assertThat(paymentOutboxEntity.getId()).isEqualTo(customerOutboxMessage.getId());
        assertThat(paymentOutboxEntity.getSagaId()).isEqualTo(customerOutboxMessage.getSagaId());
        assertThat(paymentOutboxEntity.getCreatedAt()).isEqualTo(customerOutboxMessage.getCreatedAt());
        assertThat(paymentOutboxEntity.getType()).isEqualTo(customerOutboxMessage.getType());
        assertThat(paymentOutboxEntity.getPayload()).isEqualTo(customerOutboxMessage.getPayload());
        assertThat(paymentOutboxEntity.getOutboxStatus()).isEqualTo(customerOutboxMessage.getOutboxStatus());
        assertThat(paymentOutboxEntity.getPaymentStatus()).isEqualTo(customerOutboxMessage.getPaymentStatus());
        assertThat(paymentOutboxEntity.getVersion()).isEqualTo(customerOutboxMessage.getVersion());

    }

    @Test
    void test_toCustomerOutboxMessage_shouldReturnCustomerOutboxMessage() {

        var paymentOutboxEntity = PaymentOutboxEntity.builder()
                .id(UUID.randomUUID())
                .sagaId(UUID.randomUUID())
                .createdAt(ZonedDateTime.now())
                .type("type")
                .payload("payload")
                .outboxStatus(OutboxStatus.STARTED)
                .paymentStatus(PaymentStatus.COMPLETED)
                .version(1)
                .build();

        var outboxDataHelper = new OutboxDataHelper();

        var customerOutboxMessage = outboxDataHelper.toCustomerOutboxMessage(paymentOutboxEntity);

        assertThat(customerOutboxMessage.getId()).isEqualTo(paymentOutboxEntity.getId());
        assertThat(customerOutboxMessage.getSagaId()).isEqualTo(paymentOutboxEntity.getSagaId());
        assertThat(customerOutboxMessage.getCreatedAt()).isEqualTo(paymentOutboxEntity.getCreatedAt());
        assertThat(customerOutboxMessage.getType()).isEqualTo(paymentOutboxEntity.getType());
        assertThat(customerOutboxMessage.getPayload()).isEqualTo(paymentOutboxEntity.getPayload());
        assertThat(customerOutboxMessage.getOutboxStatus()).isEqualTo(paymentOutboxEntity.getOutboxStatus());
        assertThat(customerOutboxMessage.getPaymentStatus()).isEqualTo(paymentOutboxEntity.getPaymentStatus());
        assertThat(customerOutboxMessage.getVersion()).isEqualTo(paymentOutboxEntity.getVersion());

    }
}
