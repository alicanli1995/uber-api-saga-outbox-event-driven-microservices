package com.uber.api.payment.service.messaging.service.impl;

import com.uber.api.common.api.constants.TransactionStatus;
import com.uber.api.common.api.constants.TransactionType;
import com.uber.api.common.api.dto.CallTaxiEventPayload;
import com.uber.api.payment.service.entity.BalanceHistory;
import com.uber.api.payment.service.messaging.service.ProcessCompletedMessageListener;
import com.uber.api.payment.service.repository.BalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessCompletedMessageListenerImpl implements ProcessCompletedMessageListener {

    private final BalanceRepository balanceRepository;

    @Override
    public void processCompleteAndTransferMoneyDriver(CallTaxiEventPayload driverCallRequestAvroModelToPaymentRequest) {
        log.info("Processing payment for request id: {}", driverCallRequestAvroModelToPaymentRequest.requestId());
        balanceRepository.findByMail(driverCallRequestAvroModelToPaymentRequest.driverEmail())
                .ifPresent(balance -> {
                    balance.setTotalCreditAmount(balance.getTotalCreditAmount().add(
                            BigDecimal.valueOf(driverCallRequestAvroModelToPaymentRequest.price())));
                    if (Objects.isNull(balance.getBalanceHistory())) {
                        balance.setBalanceHistory(List.of(BalanceHistory.builder()
                                        .balance(balance)
                                        .transactionAmount(BigDecimal.valueOf(driverCallRequestAvroModelToPaymentRequest.price()))
                                        .transactionDate(ZonedDateTime.now())
                                        .email(driverCallRequestAvroModelToPaymentRequest.driverEmail())
                                        .transactionType(TransactionType.DEBIT)
                                        .transactionStatus(TransactionStatus.ACCEPTED)
                                .build()
                        ));
                    }else {
                        balance.getBalanceHistory().add(BalanceHistory.builder()
                                .transactionAmount(BigDecimal.valueOf(driverCallRequestAvroModelToPaymentRequest.price()))
                                .transactionDate(ZonedDateTime.now())
                                .balance(balance)
                                .email(driverCallRequestAvroModelToPaymentRequest.driverEmail())
                                .transactionType(TransactionType.DEBIT)
                                .transactionStatus(TransactionStatus.ACCEPTED)
                                .build());
                    }
                    balanceRepository.save(balance);
                });
    }
}
