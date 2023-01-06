package com.uber.api.payment.service.outbox.helper;

import com.uber.api.common.api.constants.PaymentStatus;
import com.uber.api.outbox.OutboxStatus;
import com.uber.api.payment.service.dto.CustomerOutboxMessage;
import com.uber.api.payment.service.entity.PaymentOutboxEntity;
import com.uber.api.payment.service.repository.PaymentOutboxRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
@ExtendWith(SpringExtension.class)
@Import(OutboxHelper.class)
class OutboxHelperTest {

    @Autowired
    private OutboxHelper outboxHelper;

    @MockBean
    private OutboxDataHelper outboxDataHelper;

    @MockBean
    private PaymentOutboxRepository paymentOutboxRepository;

    @Test
    void test_updateOutboxMessage_WhenCustomerOutboxMessageIsNotNull() {

        CustomerOutboxMessage customerOutboxMessage = CustomerOutboxMessage.builder()
                .processedAt(ZonedDateTime.now())
                .outboxStatus(OutboxStatus.STARTED)
                .build();

        outboxHelper.updateOutboxMessage(customerOutboxMessage, OutboxStatus.COMPLETED);

        verify(paymentOutboxRepository, times(1))
                .save(outboxDataHelper.toCustomerOutboxEntity(customerOutboxMessage));

        assertThat(customerOutboxMessage.getOutboxStatus()).isEqualTo(OutboxStatus.COMPLETED);
        assertThat(customerOutboxMessage.getProcessedAt()).isNotNull();

    }

    @Test
    void test_customerOutboxEntityToCustomerOutboxMessage_WhenCustomerOutboxEntityIsNotNull() {

        CustomerOutboxMessage customerOutboxMessage = CustomerOutboxMessage.builder()
                .id(UUID.fromString("d0c1dc12-5757-49e3-855e-7f5e38b838f6"))
                .sagaId(UUID.fromString("d0c1dc12-5757-49e3-855e-7f5e38b838f6"))
                .paymentStatus(PaymentStatus.COMPLETED)
                .type("type")
                .outboxStatus(OutboxStatus.STARTED)
                .payload("payload")
                .createdAt(ZonedDateTime.now())
                .build();

        when(outboxDataHelper.toCustomerOutboxMessage(any())).thenReturn(customerOutboxMessage);

        var returnObj = outboxHelper.customerOutboxEntityToCustomerOutboxMessage(any());

        assertThat(returnObj).isNotNull();
        assertThat(returnObj.getId()).isEqualTo(customerOutboxMessage.getId());
        assertThat(returnObj.getSagaId()).isEqualTo(customerOutboxMessage.getSagaId());
        assertThat(returnObj.getPaymentStatus()).isEqualTo(customerOutboxMessage.getPaymentStatus());
        assertThat(returnObj.getType()).isEqualTo(customerOutboxMessage.getType());
        assertThat(returnObj.getOutboxStatus()).isEqualTo(customerOutboxMessage.getOutboxStatus());
        assertThat(returnObj.getPayload()).isEqualTo(customerOutboxMessage.getPayload());
        assertThat(returnObj.getCreatedAt()).isEqualTo(customerOutboxMessage.getCreatedAt());

    }
}
