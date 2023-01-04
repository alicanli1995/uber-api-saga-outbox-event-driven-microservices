package com.uber.api.customer.service.outbox.helper;

import com.uber.api.common.api.constants.CallStatus;
import com.uber.api.common.api.dto.CallTaxiEventPayload;
import com.uber.api.common.api.repository.PendingRequestRepository;
import com.uber.api.customer.service.dto.TaxiPaymentOutboxMessage;
import com.uber.api.customer.service.entity.BalanceOutboxEntity;
import com.uber.api.customer.service.entity.DriverApprovalOutbox;
import com.uber.api.customer.service.outbox.mapper.PaymentOutboxDataHelper;
import com.uber.api.customer.service.repository.BalanceOutboxRepository;
import com.uber.api.customer.service.repository.DriverApprovalOutboxRepository;
import com.uber.api.kafka.producer.KafkaMessageHelper;
import com.uber.api.outbox.OutboxStatus;
import com.uber.api.saga.SagaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.uber.api.outbox.SagaConst.CUSTOMER_PROCESSING_SAGA;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestOutboxHelper {
    private final BalanceOutboxRepository balanceOutboxRepository;
    private final PaymentOutboxDataHelper paymentOutboxDataHelper;
    private final DriverApprovalOutboxRepository driverApprovalOutboxRepository;
    private final PendingRequestRepository pendingRequestRepository;
    private final KafkaMessageHelper kafkaMessageHelper;

    public UUID savePaymentOutboxMessage(Object payload,
                                         String sagaStatus,
                                         UUID sagaId) {
        var paymentOutbox = BalanceOutboxEntity.builder()
                .id(UUID.randomUUID())
                .sagaId(sagaId)
                .createdAt(ZonedDateTime.now())
                .payload(kafkaMessageHelper.createPayload(payload))
                .outboxStatus(OutboxStatus.STARTED)
                .sagaStatus(SagaStatus.valueOf(sagaStatus))
                .type(CUSTOMER_PROCESSING_SAGA)
                .build();
        var response = balanceOutboxRepository.save(paymentOutbox);
        log.info("Outbox message id : {} saved successfully", response.getId());
        return response.getSagaId();
    }

    public Optional<List<TaxiPaymentOutboxMessage>> getPaymentOutboxMessageByOutboxMessageStatusAndSagaStatus(OutboxStatus outboxStatus,
                                                                                                              SagaStatus... sagaStatuses) {
        return Optional.of(balanceOutboxRepository.findByTypeAndOutboxStatusAndSagaStatusIn(CUSTOMER_PROCESSING_SAGA,
                        outboxStatus,
                        Arrays.asList(sagaStatuses))
                .orElseThrow(() -> new RuntimeException("Payment outbox object " +
                        "could not be found for saga type " + CUSTOMER_PROCESSING_SAGA))
                .stream()
                .map(paymentOutboxDataHelper::paymentOutboxEntityToCustomerPaymentOutboxMessage)
                .toList());
    }

    public TaxiPaymentOutboxMessage save(TaxiPaymentOutboxMessage taxiPaymentOutboxMessage) {
        return paymentOutboxDataHelper.paymentOutboxEntityToCustomerPaymentOutboxMessage(balanceOutboxRepository.save
                (paymentOutboxDataHelper.taxiPaymentOutboxMessageToPaymentOutboxEntity(taxiPaymentOutboxMessage)));
    }



    public Optional<List<DriverApprovalOutbox>> getApprovalOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus started, SagaStatus processing) {
        return driverApprovalOutboxRepository.findByTypeAndOutboxStatusAndSagaStatus(CUSTOMER_PROCESSING_SAGA, started, processing);
    }

    public DriverApprovalOutbox save(DriverApprovalOutbox driverApprovalOutbox) {
        return driverApprovalOutboxRepository.save(driverApprovalOutbox);
    }

    public void updateCallStatus(DriverApprovalOutbox driverApprovalOutbox) {
        pendingRequestRepository.findByRequestId(kafkaMessageHelper.getEventOnPayload(driverApprovalOutbox.getPayload(),
                        CallTaxiEventPayload.class).requestId())
                .ifPresent(pendingRequestEntity -> {
                    pendingRequestEntity.setCallStatus(CallStatus.COMPLETED);
                    pendingRequestRepository.save(pendingRequestEntity);
                });
    }

    public void updateSagaAndOutboxStatusForBalanceOutboxMessage(UUID sagaId, SagaStatus sagaStatus) {
        balanceOutboxRepository.findBySagaId(sagaId)
                .ifPresent(balanceOutboxEntity -> {
                    balanceOutboxEntity.setSagaStatus(sagaStatus);
                    balanceOutboxRepository.save(balanceOutboxEntity);
                });
    }

    public void updateSagaAndOutboxStatusForDriverOutbox(UUID sagaId, SagaStatus sagaStatus) {
        driverApprovalOutboxRepository.findBySagaId(sagaId)
                .ifPresent(driverApprovalOutbox -> {
                    driverApprovalOutbox.setSagaStatus(sagaStatus);
                    driverApprovalOutboxRepository.save(driverApprovalOutbox);
                });
    }
}
