package com.uber.api.payment.service.repository;

import com.uber.api.common.api.constants.PaymentStatus;
import com.uber.api.outbox.OutboxStatus;
import com.uber.api.payment.service.entity.PaymentOutboxEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class PaymentOutboxRepositoryTest {

    @Autowired
    private PaymentOutboxRepository paymentOutboxRepository;

    @Test
    void test_findByTypeAndSagaIdAndPaymentStatusAndOutboxStatus_with_valid_params() {
        paymentOutboxRepository.save(PaymentOutboxEntity.builder()
                .id(UUID.randomUUID())
                .type("customer_processing_saga")
                .sagaId(UUID.fromString("d0c1dc12-5757-49e3-855e-7f5e38b838f6"))
                .paymentStatus(PaymentStatus.COMPLETED)
                .outboxStatus(OutboxStatus.COMPLETED)
                .build()
        );

        Optional<PaymentOutboxEntity> customerProcessingSaga = paymentOutboxRepository.findByTypeAndSagaIdAndPaymentStatusAndOutboxStatus(
                "customer_processing_saga",
                UUID.fromString("d0c1dc12-5757-49e3-855e-7f5e38b838f6"),
                PaymentStatus.COMPLETED,
                OutboxStatus.COMPLETED);

        assertThat(customerProcessingSaga).isNotNull().isPresent();
        assertThat(customerProcessingSaga.get().getType()).isEqualTo("customer_processing_saga");
        assertThat(customerProcessingSaga.get().getSagaId()).isNotNull();
        assertThat(customerProcessingSaga.get().getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(customerProcessingSaga.get().getOutboxStatus()).isEqualTo(OutboxStatus.COMPLETED);

    }

    @Test
    void test_findByTypeAndOutboxStatus_with_valid_params() {
        paymentOutboxRepository.save(PaymentOutboxEntity.builder()
                .id(UUID.randomUUID())
                .type("customer_processing_saga")
                .sagaId(UUID.fromString("d0c1dc12-5757-49e3-855e-7f5e38b838f6"))
                .paymentStatus(PaymentStatus.COMPLETED)
                .outboxStatus(OutboxStatus.COMPLETED)
                .build()
        );

        var customerProcessingSaga = paymentOutboxRepository.findByTypeAndOutboxStatus(
                "customer_processing_saga",
                OutboxStatus.COMPLETED);

        assertThat(customerProcessingSaga).isNotNull().isPresent();
        assertThat(customerProcessingSaga.get().get(0).getType()).isEqualTo("customer_processing_saga");
        assertThat(customerProcessingSaga.get().get(0).getSagaId()).isNotNull();
        assertThat(customerProcessingSaga.get().get(0).getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(customerProcessingSaga.get().get(0).getOutboxStatus()).isEqualTo(OutboxStatus.COMPLETED);
    }


}
