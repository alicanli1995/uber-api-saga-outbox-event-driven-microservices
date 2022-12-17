package com.uber.api.payment.service.service.impl;

import com.uber.api.payment.service.exception.PaymentNotFoundException;
import com.uber.api.payment.service.repository.BalanceRepository;
import com.uber.api.payment.service.service.BalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

    private final BalanceRepository balanceRepository;

    @Override
    public BigDecimal getBalance(String mail) {
        return balanceRepository.findByMail(mail).orElseThrow(
                () -> new PaymentNotFoundException("Balance not found for mail: " + mail)
        ).getTotalCreditAmount();
    }
}
