package com.uber.api.customer.service.outbox.scheduler;

import com.uber.api.customer.service.entity.DriverApprovalOutbox;
import com.uber.api.customer.service.messaging.kafka.service.ProcessCompleteMessagePublisher;
import com.uber.api.customer.service.outbox.helper.RequestOutboxHelper;
import com.uber.api.outbox.OutboxScheduler;
import com.uber.api.outbox.OutboxStatus;
import com.uber.api.saga.SagaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessCompleteOutboxScheduler implements OutboxScheduler {

     private final ProcessCompleteMessagePublisher processCompleteMessagePublisher;
    private final RequestOutboxHelper callOutboxHelper;


    @Override
    @Transactional
    @Scheduled(fixedDelayString = "${customer-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${customer-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {
        var outboxMessages = callOutboxHelper.getApprovalOutboxMessageByOutboxStatusAndSagaStatus(
                        OutboxStatus.COMPLETED,
                        SagaStatus.COMPENSATED)
                .orElseThrow(
                        () -> new RuntimeException("No outbox message found for processing"));

        if (Objects.nonNull(outboxMessages) && !outboxMessages.isEmpty()) {

            log.info("Received {} CallOutboxMessage with ids :  {} , sending message bus !" ,
                    outboxMessages.size(),
                    outboxMessages.stream().map(callOutboxMessage -> callOutboxMessage.getId().toString())
                            .collect(Collectors.joining(",")));
            outboxMessages.forEach(callOutboxMessage -> {
                        updateCallStatus(callOutboxMessage);
                        processCompleteMessagePublisher.publish(callOutboxMessage,this::updateOutboxStatus);
                    });
        }
    }

    private void updateCallStatus(DriverApprovalOutbox driverApprovalOutbox) {
        callOutboxHelper.updateCallStatus(driverApprovalOutbox);
        driverApprovalOutbox.setSagaStatus(SagaStatus.SUCCEEDED);
    }

    private void updateOutboxStatus(DriverApprovalOutbox driverApprovalOutbox, OutboxStatus outboxStatus) {
            driverApprovalOutbox.setOutboxStatus(outboxStatus);
            callOutboxHelper.save(driverApprovalOutbox);
    }


}
