package com.uber.api.customer.service.repository;

import com.uber.api.customer.service.entity.BalanceOutboxEntity;
import com.uber.api.outbox.OutboxStatus;
import com.uber.api.saga.SagaStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BalanceOutboxRepository extends JpaRepository<BalanceOutboxEntity, UUID> {
    Optional<List<BalanceOutboxEntity>> findByTypeAndOutboxStatusAndSagaStatusIn(String customerProcessingSaga,
                                                                                 OutboxStatus outboxStatus,
                                                                                 List<SagaStatus> asList);

    Optional<BalanceOutboxEntity> findByTypeAndSagaIdAndSagaStatusIn(String type,
                                                                     UUID sagaId,
                                                                     List<SagaStatus> sagaStatus);

    Optional<BalanceOutboxEntity> findBySagaId(UUID sagaId);

}
