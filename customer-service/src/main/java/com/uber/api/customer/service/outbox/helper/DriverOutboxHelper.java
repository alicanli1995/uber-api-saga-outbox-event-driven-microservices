package com.uber.api.customer.service.outbox.helper;

import com.uber.api.common.api.constants.CustomerStatus;
import com.uber.api.common.api.dto.CallTaxiEventPayload;
import com.uber.api.customer.service.entity.BalanceOutboxEntity;
import com.uber.api.customer.service.entity.DriverApprovalOutbox;
import com.uber.api.customer.service.repository.BalanceOutboxRepository;
import com.uber.api.customer.service.repository.DriverApprovalOutboxRepository;
import com.uber.api.kafka.producer.KafkaMessageHelper;
import com.uber.api.outbox.OutboxStatus;
import com.uber.api.saga.SagaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static com.uber.api.outbox.SagaConst.CUSTOMER_PROCESSING_SAGA;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverOutboxHelper {
    private final KafkaMessageHelper kafkaMessageHelper;
    private final DriverApprovalOutboxRepository driverApprovalOutboxRepository;
    private final BalanceOutboxRepository balanceOutboxRepository;
    public void saveDriverOutboxMessage(CallTaxiEventPayload customerPaidEventToTaxiCallEventPayload,
                                        SagaStatus sagaStatus,
                                        OutboxStatus started,
                                        UUID fromString) {

        driverApprovalOutboxRepository.save(getDriverApprovalOutbox(customerPaidEventToTaxiCallEventPayload,
                sagaStatus,
                started,
                fromString));

    }

    private DriverApprovalOutbox getDriverApprovalOutbox(CallTaxiEventPayload customerPaidEventToTaxiCallEventPayload,
                                                         SagaStatus sagaStatus,
                                                         OutboxStatus started,
                                                         UUID fromString) {

        return DriverApprovalOutbox.builder()
                .id(UUID.randomUUID())
                .sagaId(fromString)
                .outboxStatus(started)
                .sagaStatus(sagaStatus)
                .createdAt(ZonedDateTime.now())
                .type(CUSTOMER_PROCESSING_SAGA)
                .payload(kafkaMessageHelper.createPayload(customerPaidEventToTaxiCallEventPayload))
                .build();

    }




    public DriverApprovalOutbox getAndUpdateDriverApprovedOutboxMessage(DriverApprovalOutbox approvalOutbox, SagaStatus sagaStatus) {
        DriverApprovalOutbox driverApprovalOutbox = driverApprovalOutboxRepository.findBySagaId(approvalOutbox.getSagaId())
                .orElseThrow(() -> new RuntimeException("Driver approval outbox not found for saga id " + approvalOutbox.getSagaId()));
        driverApprovalOutbox.setSagaStatus(sagaStatus);
        driverApprovalOutbox.setOutboxStatus(OutboxStatus.COMPLETED);
        driverApprovalOutbox.setProcessedAt(ZonedDateTime.now(ZoneId.of("UTC")));
        return driverApprovalOutboxRepository.save(driverApprovalOutbox);
    }

    public BalanceOutboxEntity getAndUpdateBalanceOutboxMessage(OutboxStatus status,
                                                                UUID balanceOutboxEntitySagaId,
                                                                SagaStatus sagaStatus,
                                                                CustomerStatus customerStatus) {
        BalanceOutboxEntity outboxEntity = balanceOutboxRepository.findBySagaId(balanceOutboxEntitySagaId)
                .orElseThrow(() -> new RuntimeException("Balance outbox not found for saga id " + balanceOutboxEntitySagaId));

        outboxEntity.setSagaStatus(sagaStatus);
        outboxEntity.setOutboxStatus(status);
        outboxEntity.setProcessedAt(ZonedDateTime.now(ZoneId.of("UTC")));
        return balanceOutboxRepository.save(outboxEntity);
    }
}
