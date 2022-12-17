package com.uber.api.payment.service.outbox.helper;

import com.uber.api.common.api.constants.PaymentStatus;
import com.uber.api.common.api.entity.PendingRequest;
import com.uber.api.common.api.exception.PendingRequestNotFoundException;
import com.uber.api.common.api.repository.PendingRequestRepository;
import com.uber.api.outbox.OutboxStatus;
import com.uber.api.payment.service.dto.CallEventPayload;
import com.uber.api.payment.service.dto.CustomerOutboxMessage;
import com.uber.api.payment.service.dto.Payment;
import com.uber.api.payment.service.dto.PaymentRequest;
import com.uber.api.payment.service.entity.Balance;
import com.uber.api.payment.service.exception.PaymentNotFoundException;
import com.uber.api.payment.service.messaging.helper.PaymentMessagingDataMapper;
import com.uber.api.payment.service.repository.BalanceRepository;
import com.uber.api.payment.service.repository.PaymentOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOutboxHelper {
    private final PaymentOutboxRepository paymentOutboxRepository;
    private final PaymentMessagingDataMapper paymentMessagingDataMapper;
    private final BalanceRepository balanceRepository;
    private final PendingRequestRepository pendingRequestRepository;

    public Optional<CustomerOutboxMessage> getCompletedCustomerOutboxMessageBySagaIdAndPaymentStatus(String customerProcessingSaga,
                                                                                                     UUID fromString,
                                                                                                     PaymentStatus paymentStatus, OutboxStatus completed) {

        return paymentOutboxRepository.findByTypeAndSagaIdAndPaymentStatusAndOutboxStatus(customerProcessingSaga, fromString,
                        paymentStatus, completed)
                .map(paymentMessagingDataMapper::customerOutboxEntityToCustomerOutboxMessage);

    }

    public void updateOutboxMessage(CustomerOutboxMessage customerOutboxMessage, OutboxStatus outboxStatus) {
        customerOutboxMessage.setOutboxStatus(outboxStatus);
        paymentOutboxRepository
                .save(paymentMessagingDataMapper.customerOutboxMessageToCustomerOutboxEntity(customerOutboxMessage));
    }

    public Payment paymentRequestModelToPayment(PaymentRequest paymentRequest) {
        return Payment.builder()
                .id(UUID.randomUUID())
                .customerMail(paymentRequest.getCustomerMail())
                .requestId(paymentRequest.getRequestId())
                .price(paymentRequest.getPrice())
                .build();
    }

    public Balance getBalanceByMail(String customerMail) {
        return balanceRepository.findByMail(customerMail)
                .orElseThrow(() -> new PaymentNotFoundException("No credit entry found for customer mail: " + customerMail));
    }

    public void saveCustomerOutboxMessage(CallEventPayload callEventPayload,
                                          PaymentStatus status,
                                          OutboxStatus started,
                                          UUID fromString) {
        paymentOutboxRepository.save(paymentMessagingDataMapper
                .paymentEventToCustomerOutboxEntity(callEventPayload, status, started, fromString));

    }

    public void saveBalance(Balance creditEntry) {
        balanceRepository.save(creditEntry);
    }

    public PendingRequest findByRequestId(UUID fromString) {
        return pendingRequestRepository.findByRequestId(fromString).orElseThrow(
                () -> new PendingRequestNotFoundException("No pending request found for request id: " + fromString));
    }

    public void savePendingRequest(PendingRequest pendingRequest) {
        pendingRequestRepository.save(pendingRequest);
    }
}
