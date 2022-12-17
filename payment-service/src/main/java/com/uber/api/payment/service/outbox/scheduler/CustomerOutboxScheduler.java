package com.uber.api.payment.service.outbox.scheduler;


import com.uber.api.outbox.OutboxScheduler;
import com.uber.api.outbox.OutboxStatus;
import com.uber.api.payment.service.entity.PaymentOutboxEntity;
import com.uber.api.payment.service.messaging.kafka.publisher.PaymentResponseMessagePublisher;
import com.uber.api.payment.service.outbox.helper.OutboxHelper;
import com.uber.api.payment.service.repository.PaymentOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.uber.api.outbox.SagaConst.CUSTOMER_PROCESSING_SAGA;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerOutboxScheduler implements OutboxScheduler {
    private final OutboxHelper outboxHelper;
    private final PaymentResponseMessagePublisher paymentResponseMessagePublisher;
    private final PaymentOutboxRepository paymentOutboxRepository;

    @Override
    @Transactional
    @Scheduled(fixedRateString = "${payment-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${payment-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {

        Optional<List<PaymentOutboxEntity>> customerOutboxEntities = paymentOutboxRepository
                .findByTypeAndOutboxStatus(CUSTOMER_PROCESSING_SAGA, OutboxStatus.STARTED);

        log.info("Received {} CustomerOutboxMessage with ids {}, sending to message bus!", customerOutboxEntities.get().size(),
                customerOutboxEntities.get().stream().map(outboxMessage ->
                        outboxMessage.getId().toString()).collect(Collectors.joining(",")));

        customerOutboxEntities.get().forEach(customerOutboxEntity ->
                paymentResponseMessagePublisher.publish(outboxHelper.customerOutboxEntityToCustomerOutboxMessage(customerOutboxEntity),
                        outboxHelper::updateOutboxMessage));

        log.info("{} CustomerOutboxMessage sent to message bus!", customerOutboxEntities.get().size());
    }
}
