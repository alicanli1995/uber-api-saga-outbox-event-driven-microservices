package com.uber.api.payment.service.messaging.service.impl;

import com.uber.api.common.api.constants.PaymentStatus;
import com.uber.api.common.api.constants.TransactionStatus;
import com.uber.api.common.api.constants.TransactionType;
import com.uber.api.payment.service.dto.Payment;
import com.uber.api.payment.service.entity.Balance;
import com.uber.api.payment.service.entity.BalanceHistory;
import com.uber.api.payment.service.event.PaymentCompletedEvent;
import com.uber.api.payment.service.event.PaymentEvent;
import com.uber.api.payment.service.event.PaymentFailedEvent;
import com.uber.api.payment.service.messaging.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Override
    public PaymentEvent validateAndInitializePayment(Payment payment, Balance balance, List<String> failureMessage) {
        payment.validatePayment(failureMessage);
        payment.initializePayment();
        validateCreditEntry(payment,balance,failureMessage);

        if (failureMessage.isEmpty()) {
            log.info("Payment is valid and initialized");
            payment.setStatus(PaymentStatus.COMPLETED);
            subtractCreditEntry(payment,balance);
            addPaymentToBalanceHistory(balance,payment,TransactionType.DEBIT);
            return new PaymentCompletedEvent(payment, LocalDateTime.now(ZoneId.of("UTC")));
        }
        else {
            log.info("Payment is invalid and not initialized");
            payment.setStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(payment, LocalDateTime.now(ZoneId.of("UTC")), failureMessage);
        }

    }

    @Override
    public PaymentEvent validateAndCancelPayment(Payment payment, Balance creditEntry, List<String> failureMessage) {
        payment.validatePayment(failureMessage);
        addCreditEntry(payment,creditEntry);
        addPaymentToBalanceHistory(creditEntry,payment,TransactionType.CREDIT);

        if (failureMessage.isEmpty()) {
            log.info("Payment is valid and cancelled");
            payment.setStatus(PaymentStatus.CANCELED);
            return new PaymentCompletedEvent(payment, LocalDateTime.now(ZoneId.of("UTC")));
        }
        else {
            log.info("Payment is invalid and not cancelled");
            payment.setStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(payment, LocalDateTime.now(ZoneId.of("UTC")), failureMessage);
        }
    }

    private void addCreditEntry(Payment payment, Balance creditEntry) {
        var newBalance = creditEntry.getTotalCreditAmount().add(payment.getPrice());
        creditEntry.setTotalCreditAmount(newBalance);
    }

    private void addPaymentToBalanceHistory(Balance balance, Payment payment, TransactionType transactionType) {
        var newBalanceHistory = BalanceHistory.builder()
                .balance(balance)
                .email(payment.getCustomerMail())
                .transactionAmount(payment.getPrice())
                .transactionStatus(TransactionStatus.ACCEPTED)
                .transactionType(transactionType)
                .transactionDate(ZonedDateTime.now(ZoneId.of("UTC")))
                .build();

        balance.getBalanceHistory().add(newBalanceHistory);
    }


    private void subtractCreditEntry(Payment payment, Balance creditEntry) {
        BigDecimal newBalance = creditEntry.getTotalCreditAmount().subtract(payment.getPrice());
        creditEntry.setTotalCreditAmount(newBalance);
    }

    private void validateCreditEntry(Payment payment, Balance creditEntry, List<String> failureMessage) {
        if (creditEntry.getTotalCreditAmount().compareTo(payment.getPrice()) < 0) {
            failureMessage.add("Insufficient balance");
        }
    }
}
