package com.uber.api.driver.api.outbox.scheduler;

import com.uber.api.driver.api.entity.CustomerRequestOutboxEntity;
import com.uber.api.driver.api.messaging.kafka.publisher.CustomerAcceptResponseMessagePublisher;
import com.uber.api.driver.api.outbox.helper.DriverRequestOutboxHelper;
import com.uber.api.outbox.OutboxScheduler;
import com.uber.api.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DriverOutboxScheduler implements OutboxScheduler {

    private final DriverRequestOutboxHelper driverAcceptOutboxHelper;
    private final CustomerAcceptResponseMessagePublisher responseMessagePublisher;


    @Transactional
    @Scheduled(fixedRateString = "${driver-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${driver-service.outbox-scheduler-initial-delay}")
    @Override
    public void processOutboxMessage() {
        Optional<List<CustomerRequestOutboxEntity>> outboxMessagesResponse =
                driverAcceptOutboxHelper.getRequestOutboxMessageByOutboxStatus(OutboxStatus.STARTED);

        if (outboxMessagesResponse.isPresent() && !outboxMessagesResponse.get().isEmpty()) {
            List<CustomerRequestOutboxEntity> outboxMessages = outboxMessagesResponse.get();
            log.info("Received {} OrderOutboxMessage with ids {}, sending to message bus!", outboxMessages.size(),
                    outboxMessages.stream().map(outboxMessage ->
                            outboxMessage.getId().toString()).collect(Collectors.joining(",")));
            outboxMessages.forEach(orderOutboxMessage -> responseMessagePublisher.publish(orderOutboxMessage,
                    driverAcceptOutboxHelper::updateOutboxStatus));
            log.info("{} OrderOutboxMessage sent to message bus!", outboxMessages.size());
        }
    }
}
