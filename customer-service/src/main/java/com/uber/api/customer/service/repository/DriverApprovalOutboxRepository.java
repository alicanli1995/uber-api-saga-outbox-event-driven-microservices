package com.uber.api.customer.service.repository;

import com.uber.api.customer.service.entity.DriverApprovalOutbox;
import com.uber.api.outbox.OutboxStatus;
import com.uber.api.saga.SagaStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverApprovalOutboxRepository extends JpaRepository<DriverApprovalOutbox, UUID> {
    Optional<List<DriverApprovalOutbox>> findByTypeAndOutboxStatusAndSagaStatus(String customerProcessingSaga,
                                                                                OutboxStatus started,
                                                                                SagaStatus processing);
    Optional<DriverApprovalOutbox> findBySagaId(UUID sagaId);

    Optional<DriverApprovalOutbox> findByTypeAndSagaIdAndSagaStatus(String customerProcessingSaga,
                                                                    UUID fromString,
                                                                    SagaStatus processing);
}