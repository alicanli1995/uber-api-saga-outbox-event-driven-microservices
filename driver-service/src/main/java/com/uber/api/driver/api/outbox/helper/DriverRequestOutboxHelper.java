package com.uber.api.driver.api.outbox.helper;

import com.uber.api.common.api.constants.CallStatus;
import com.uber.api.common.api.repository.PendingRequestRepository;
import com.uber.api.driver.api.dto.CallApprovedEventPayload;
import com.uber.api.driver.api.dto.DriverCallRequestDTO;
import com.uber.api.driver.api.entity.CustomerRequestOutboxEntity;
import com.uber.api.driver.api.event.CallRequestApprovalEvent;
import com.uber.api.driver.api.repository.CustomerRequestOutboxEntityRepository;
import com.uber.api.kafka.producer.KafkaMessageHelper;
import com.uber.api.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.uber.api.outbox.SagaConst.CUSTOMER_PROCESSING_SAGA;

@Slf4j
@Component
@RequiredArgsConstructor
public class DriverRequestOutboxHelper {

    private final KafkaMessageHelper kafkaMessageHelper;
    private final CustomerRequestOutboxEntityRepository customerRequestOutboxEntityRepository;
    private final PendingRequestRepository pendingRequestRepository;

    @Transactional
    public void saveRequestOutboxMessage(DriverCallRequestDTO driverCallRequestAvroModelToCallDriverDTO,
                                         CallRequestApprovalEvent callRequestApprovalEvent) {

        customerRequestOutboxEntityRepository.save(CustomerRequestOutboxEntity.builder()
                        .id(UUID.randomUUID())
                        .sagaId(UUID.fromString(driverCallRequestAvroModelToCallDriverDTO.getSagaId()))
                        .type(CUSTOMER_PROCESSING_SAGA)
                        .outboxStatus(OutboxStatus.STARTED)
                        .createdAt(driverCallRequestAvroModelToCallDriverDTO.getCreatedAt())
                        .processedAt(ZonedDateTime.now(ZoneId.of("UTC")))
                        .payload(kafkaMessageHelper.createPayload(getPayload(callRequestApprovalEvent)))
                .build());

        log.info("Driver call request outbox message saved for request id: {}", driverCallRequestAvroModelToCallDriverDTO.getRequestId());

    }

    private CallApprovedEventPayload getPayload(CallRequestApprovalEvent callRequestApprovalEvent) {
        return CallApprovedEventPayload.builder()
                .requestId(String.valueOf(callRequestApprovalEvent.getDriver().getPendingRequest().getRequestId()))
                .customerLocation(callRequestApprovalEvent.getDriver().getPendingRequest().getCustomerLocation())
                .customerDestination(callRequestApprovalEvent.getDriver().getPendingRequest().getCustomerDestination())
                .customerEmail(callRequestApprovalEvent.getDriver().getPendingRequest().getCustomerEmail())
                .driverEmail(callRequestApprovalEvent.getDriver().getEmail())
                .distance(callRequestApprovalEvent.getDriver().getPendingRequest().getCustomerDestination().getDistance())
                .build();
    }

    public Optional<List<CustomerRequestOutboxEntity>> getRequestOutboxMessageByOutboxStatus(OutboxStatus started) {
        var entities =  customerRequestOutboxEntityRepository
                .findByTypeAndOutboxStatus(CUSTOMER_PROCESSING_SAGA,started);
        if (!entities.isEmpty()){
            return Optional.of(entities.stream()
                    .filter(entity -> {
                        var pendingRequest = pendingRequestRepository.findByRequestId(UUID.fromString(kafkaMessageHelper.getEventOnPayload(
                                entity.getPayload(), CallApprovedEventPayload.class).getRequestId()));
                        return pendingRequest.map(request -> request.getCallStatus().equals(CallStatus.ACCEPTED) ||
                                request.getCallStatus().equals(CallStatus.DRIVER_REJECTED)).orElse(false);
                    })
                    .toList());
        }else return Optional.of(entities);
    }

    public void updateOutboxStatus(CustomerRequestOutboxEntity customerRequestOutboxEntity, OutboxStatus outboxStatus) {
        customerRequestOutboxEntity.setOutboxStatus(outboxStatus);
        customerRequestOutboxEntityRepository.save(customerRequestOutboxEntity);
    }


}
