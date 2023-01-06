package com.uber.api.payment.service.outbox.helper;

import com.uber.api.outbox.OutboxStatus;
import com.uber.api.payment.service.dto.CustomerOutboxMessage;
import com.uber.api.payment.service.entity.PaymentOutboxEntity;
import com.uber.api.payment.service.repository.PaymentOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxHelper {

    private final PaymentOutboxRepository paymentOutboxRepository;
    private final OutboxDataHelper outboxDataHelper;

    @Transactional
    public void updateOutboxMessage(CustomerOutboxMessage customerOutboxMessage, OutboxStatus outboxStatus) {
        customerOutboxMessage.setOutboxStatus(outboxStatus);
        paymentOutboxRepository.save(outboxDataHelper.toCustomerOutboxEntity(customerOutboxMessage));
        log.info("CustomerOutboxMessage with id {} updated with status {}", customerOutboxMessage.getId(), outboxStatus);
    }

    public CustomerOutboxMessage customerOutboxEntityToCustomerOutboxMessage(PaymentOutboxEntity paymentOutboxEntity) {
        return outboxDataHelper.toCustomerOutboxMessage(paymentOutboxEntity);
    }
}
