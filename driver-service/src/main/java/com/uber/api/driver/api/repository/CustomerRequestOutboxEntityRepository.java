package com.uber.api.driver.api.repository;

import com.uber.api.driver.api.entity.CustomerRequestOutboxEntity;
import com.uber.api.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRequestOutboxEntityRepository extends JpaRepository<CustomerRequestOutboxEntity, UUID> {
    Optional<CustomerRequestOutboxEntity> findByTypeAndSagaIdAndOutboxStatus(String type,
                                                                             UUID sagaId,
                                                                             OutboxStatus outboxStatus);


    List<CustomerRequestOutboxEntity> findByTypeAndOutboxStatus(String customerProcessingSaga,
                                                                          OutboxStatus started);

    CustomerRequestOutboxEntity findBySagaId(UUID sagaId);


}