package com.uber.api.customer.service.outbox.scheduler;

import com.uber.api.common.api.constants.CustomerStatus;
import com.uber.api.customer.service.messaging.kafka.service.PaymentRequestMessagePublisher;
import com.uber.api.customer.service.dto.TaxiPaymentOutboxMessage;
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
public class PaymentOutboxScheduler implements OutboxScheduler {

    private final RequestOutboxHelper requestOutboxHelper;
    private final PaymentRequestMessagePublisher paymentRequestMessagePublisher;

    @Override
    @Transactional
    @Scheduled(fixedDelayString = "${customer-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${customer-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {

        log.info("Processing outbox message STARTED !");

        var outboxMessageResponse =
                requestOutboxHelper.getPaymentOutboxMessageByOutboxMessageStatusAndSagaStatus(
                        OutboxStatus.STARTED,
                        SagaStatus.STARTED,
                        SagaStatus.COMPENSATING,
                                SagaStatus.PROCESSING)
                        .orElseThrow(
                                () -> new RuntimeException("No outbox message found for processing"));

        if (Objects.nonNull(outboxMessageResponse) && !outboxMessageResponse.isEmpty()) {

            log.info("Received {} CustomerPaymentOutboxMessage with ids :  {} , sending message bus !" ,
                    outboxMessageResponse.size(),
                    outboxMessageResponse.stream().map(taxiPaymentOutboxMessage -> taxiPaymentOutboxMessage.getId().toString())
                            .collect(Collectors.joining(",")));
            outboxMessageResponse.forEach(paymentOutboxMessage -> {
                if (paymentOutboxMessage.getSagaStatus().equals(SagaStatus.PROCESSING)){
                    paymentRequestMessagePublisher.
                            publish(paymentOutboxMessage,
                            this::updateOutboxStatus);
                    updateOutboxAndSagaStatusForDriverRejected(paymentOutboxMessage);
                }else {
                    paymentRequestMessagePublisher.publish(paymentOutboxMessage,
                            this::updateOutboxStatus);
                }
            });
        }
        log.info("Processing outbox message completed ! ");
    }

    private void updateOutboxAndSagaStatusForDriverRejected(TaxiPaymentOutboxMessage paymentOutboxMessage) {
        requestOutboxHelper.updateSagaAndOutboxStatusForBalanceOutboxMessage(paymentOutboxMessage.getId(),SagaStatus.FAILED);
        requestOutboxHelper.updateSagaAndOutboxStatusForDriverOutbox(paymentOutboxMessage.getSagaId(), SagaStatus.FAILED);
    }

    private void updateOutboxStatus(TaxiPaymentOutboxMessage taxiPaymentOutboxMessage, OutboxStatus outboxStatus) {
        taxiPaymentOutboxMessage.setOutboxStatus(outboxStatus);
        requestOutboxHelper.save(taxiPaymentOutboxMessage);
    }
}
