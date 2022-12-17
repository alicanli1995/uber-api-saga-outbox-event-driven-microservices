package com.uber.api.payment.service.repository;

import com.uber.api.common.api.constants.PaymentStatus;
import com.uber.api.outbox.OutboxStatus;
import com.uber.api.payment.service.entity.PaymentOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentOutboxRepository extends JpaRepository<PaymentOutboxEntity, UUID> {
    Optional<PaymentOutboxEntity> findByTypeAndSagaIdAndPaymentStatusAndOutboxStatus(String customer_processing_saga,
                                                                                     UUID fromString,
                                                                                     PaymentStatus completed,
                                                                                     OutboxStatus completed1);

    Optional<List<PaymentOutboxEntity>> findByTypeAndOutboxStatus(String type, OutboxStatus status);
}