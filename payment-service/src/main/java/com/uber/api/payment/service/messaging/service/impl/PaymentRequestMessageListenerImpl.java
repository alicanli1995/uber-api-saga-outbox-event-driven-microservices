package com.uber.api.payment.service.messaging.service.impl;

import com.uber.api.common.api.constants.CallStatus;
import com.uber.api.common.api.constants.PaymentStatus;
import com.uber.api.common.api.entity.PendingRequest;
import com.uber.api.outbox.OutboxStatus;
import com.uber.api.payment.service.dto.PaymentRequest;
import com.uber.api.payment.service.entity.Balance;
import com.uber.api.payment.service.helper.PaymentDataMapper;
import com.uber.api.payment.service.messaging.kafka.publisher.PaymentResponseMessagePublisher;
import com.uber.api.payment.service.messaging.service.PaymentRequestMessageListener;
import com.uber.api.payment.service.messaging.service.PaymentService;
import com.uber.api.payment.service.outbox.helper.PaymentOutboxHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.uber.api.outbox.SagaConst.CUSTOMER_PROCESSING_SAGA;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRequestMessageListenerImpl implements PaymentRequestMessageListener {

    private final PaymentOutboxHelper paymentOutboxHelper;
    private final PaymentResponseMessagePublisher paymentResponseMessagePublisher;
    private final PaymentService paymentService;
    private final PaymentDataMapper paymentDataMapper;

    @Override
    @Transactional
    public void completePayment(PaymentRequest paymentRequest) {
        if (publishIfOutboxMessageProcessedForPayment(paymentRequest,PaymentStatus.COMPLETED)) {
            log.info("Outbox Message with sagaId : {} already save !", paymentRequest.getSagaId());
            return;
        }

        log.info("Received payment complete event for id : {}", paymentRequest.getRequestId());

        var payment = paymentOutboxHelper.paymentRequestModelToPayment(paymentRequest);
        var creditEntry = paymentOutboxHelper.getBalanceByMail(payment.getCustomerMail());
        List<String> failureMessage = new ArrayList<>();

        var paymentEvent = paymentService.validateAndInitializePayment
                (payment, creditEntry, failureMessage);

        persistDbObject(creditEntry,failureMessage, paymentRequest.getRequestId());

        paymentOutboxHelper.saveCustomerOutboxMessage(paymentDataMapper.paymentEventToCustomerEventPayload(paymentEvent),
                paymentEvent.getPayment().getStatus(),
                OutboxStatus.STARTED,
                UUID.fromString(paymentRequest.getSagaId()));
    }

    @Override
    public void cancelPayment(PaymentRequest paymentRequest) {

        if (publishIfOutboxMessageProcessedForPayment(paymentRequest, PaymentStatus.CANCELED)) {
            log.info("Outbox Message with sagaId : {} already save !", paymentRequest.getSagaId());
            return;
        }

        log.info("Received payment cancel event for id : {}", paymentRequest.getRequestId());

        var payment = paymentOutboxHelper.paymentRequestModelToPayment(paymentRequest);
        var creditEntry = paymentOutboxHelper.getBalanceByMail(payment.getCustomerMail());
        List<String> failureMessage = new ArrayList<>();

        var paymentEvent = paymentService.validateAndCancelPayment
                (payment, creditEntry, failureMessage);

        persistDbObject(creditEntry, failureMessage,paymentRequest.getRequestId());

        paymentOutboxHelper.saveCustomerOutboxMessage(paymentDataMapper.paymentEventToCustomerEventPayload(paymentEvent),
                paymentEvent.getPayment().getStatus(),
                OutboxStatus.STARTED,
                UUID.fromString(paymentRequest.getSagaId()));

    }

    private void persistDbObject(Balance creditEntry, List<String> failureMessage, String requestId) {
        if (failureMessage.isEmpty()) {
            paymentOutboxHelper.saveBalance(creditEntry);
        }
        else
        {
            PendingRequest pendingRequest = paymentOutboxHelper.findByRequestId(UUID.fromString(requestId));
            pendingRequest.setCallStatus(CallStatus.PAYMENT_FAILED);
            paymentOutboxHelper.savePendingRequest(pendingRequest);
        }
    }

    private boolean publishIfOutboxMessageProcessedForPayment(PaymentRequest paymentRequest,
                                                              PaymentStatus paymentStatus) {
        var outboxMessage = paymentOutboxHelper.getCompletedCustomerOutboxMessageBySagaIdAndPaymentStatus(
                CUSTOMER_PROCESSING_SAGA,UUID.fromString(paymentRequest.getSagaId()), paymentStatus, OutboxStatus.COMPLETED);
        if (outboxMessage.isPresent()) {
            paymentResponseMessagePublisher.publish(outboxMessage.get(),
                    paymentOutboxHelper::updateOutboxMessage);
            return true;
        }
        return false;
    }


}
