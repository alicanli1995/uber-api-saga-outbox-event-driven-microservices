package com.uber.api.customer.service.saga;

import com.uber.api.common.api.constants.CustomerStatus;
import com.uber.api.common.api.entity.PendingRequest;
import com.uber.api.customer.service.dto.DriverCallResponse;
import com.uber.api.customer.service.dto.TaxiPaymentEventPayload;
import com.uber.api.customer.service.entity.DriverApprovalOutbox;
import com.uber.api.customer.service.event.CustomerPaymentCancelledEvent;
import com.uber.api.customer.service.helper.CallDataMapper;
import com.uber.api.customer.service.outbox.helper.DriverOutboxHelper;
import com.uber.api.customer.service.repository.BalanceOutboxRepository;
import com.uber.api.customer.service.repository.CustomerRepository;
import com.uber.api.customer.service.repository.DriverApprovalOutboxRepository;
import com.uber.api.customer.service.saga.helper.CustomerSagaHelper;
import com.uber.api.kafka.producer.KafkaMessageHelper;
import com.uber.api.outbox.OutboxStatus;
import com.uber.api.saga.SagaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.uber.api.outbox.SagaConst.CUSTOMER_PROCESSING_SAGA;

@Slf4j
@Component
@RequiredArgsConstructor
public class DriverSaga {
    private final DriverApprovalOutboxRepository driverApprovalOutboxRepository;
    private final CallDataMapper callDataMapper;
    private final KafkaMessageHelper kafkaMessageHelper;
    private final BalanceOutboxRepository balanceOutboxRepository;
    private final CustomerRepository customerRepository;
    private final DriverOutboxHelper driverOutboxHelper;
    private final CustomerSagaHelper customerSagaHelper;

    public void process(DriverCallResponse callResponse) {
        Optional<DriverApprovalOutbox> balanceOutboxEntityOptional = driverApprovalOutboxRepository.findByTypeAndSagaIdAndSagaStatus(
                CUSTOMER_PROCESSING_SAGA, UUID.fromString(callResponse.getSagaId()), SagaStatus.COMPENSATING);


        PendingRequest pendingRequest = updateCustomerStatus(callResponse, CustomerStatus.ON_THE_WAY);

        var sagaStatus = customerSagaHelper.customerStatusToSagaStatus(pendingRequest.getCallStatus());

        if (balanceOutboxEntityOptional.isPresent()){
            driverApprovalOutboxRepository.save(driverOutboxHelper.getAndUpdateDriverApprovedOutboxMessage(
                    balanceOutboxEntityOptional.get(), sagaStatus ));

            balanceOutboxRepository.save(driverOutboxHelper.getAndUpdateBalanceOutboxMessage(
                    OutboxStatus.COMPLETED,
                    balanceOutboxEntityOptional.get().getSagaId(),
                    sagaStatus,
                    CustomerStatus.ON_THE_WAY));
            log.info("DriverSaga: Driver status updated for sagaId: {}", callResponse.getSagaId());
        }

    }
    public void rollback(DriverCallResponse response) {
        var balanceOutboxEntityOptional = driverApprovalOutboxRepository.findByTypeAndSagaIdAndSagaStatus(
                CUSTOMER_PROCESSING_SAGA, UUID.fromString(response.getSagaId()), SagaStatus.COMPENSATING).orElseThrow(
                () -> new RuntimeException("DriverSaga: No outbox message found for sagaId: " + response.getSagaId()));

        CustomerPaymentCancelledEvent event = rollbackCallRequest(response);

        var sagaStatus = customerSagaHelper.customerStatusToSagaStatus(event.getPendingRequest().getCallStatus());

        driverApprovalOutboxRepository.save(driverOutboxHelper.getAndUpdateDriverApprovedOutboxMessage(
                balanceOutboxEntityOptional, sagaStatus ));

        savePaymentOutboxMessage(callDataMapper
                        .orderCancelledEventToOrderPaymentEventPayload(event),
                sagaStatus,
                UUID.fromString(response.getSagaId()));

        log.info("DriverSaga: Driver status updated for sagaId: {}", response.getSagaId());
    }

    private void savePaymentOutboxMessage(TaxiPaymentEventPayload payload,
                                                         SagaStatus sagaStatus,
                                                         UUID sagaId) {
        balanceOutboxRepository.findBySagaId(sagaId).ifPresent(balanceOutboxEntity -> {
            balanceOutboxEntity.setSagaStatus(sagaStatus);
            balanceOutboxEntity.setPayload(kafkaMessageHelper.createPayload(payload));
            balanceOutboxEntity.setProcessedAt(ZonedDateTime.now(ZoneId.of("UTC")));
            balanceOutboxEntity.setOutboxStatus(OutboxStatus.STARTED);
            balanceOutboxRepository.save(balanceOutboxEntity);
        });
    }

    private CustomerPaymentCancelledEvent rollbackCallRequest(DriverCallResponse response) {
        PendingRequest pendingRequest = updateCustomerStatus(response, CustomerStatus.AVAILABLE);
        CustomerPaymentCancelledEvent event = new CustomerPaymentCancelledEvent(
                pendingRequest,
                ZonedDateTime.now(ZoneId.of("UTC")));
        log.info("DriverSaga: Customer status updated for sagaId: {}", response.getSagaId());
        return event;
    }

    private PendingRequest updateCustomerStatus(DriverCallResponse callResponse, CustomerStatus customerStatus) {
        var customer = customerRepository.findByPendingRequest(
                PendingRequest.builder().requestId(UUID.fromString(callResponse.getPendingRequestId())).build()).orElseThrow(
                        () -> new RuntimeException("Customer not found for pending request id: " + callResponse.getPendingRequestId()));
        customer.setCustomerStatus(customerStatus);
        customerRepository.save(customer);
        return customer.getPendingRequest();
    }


}
