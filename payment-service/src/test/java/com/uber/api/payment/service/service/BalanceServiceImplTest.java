package com.uber.api.payment.service.service;

import com.uber.api.kafka.model.UserType;
import com.uber.api.payment.service.entity.Balance;
import com.uber.api.payment.service.exception.PaymentNotFoundException;
import com.uber.api.payment.service.repository.BalanceRepository;
import com.uber.api.payment.service.service.impl.BalanceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;

@ExtendWith(SpringExtension.class)
@Import(BalanceServiceImpl.class)
class BalanceServiceImplTest {

    @Autowired
    private BalanceService balanceService;

    @MockBean
    private BalanceRepository balanceRepository;

    @Test
    void test_getBalance_with_valid_params() {
        var balance = Balance.builder()
                .mail("test@gmail.com")
                .totalCreditAmount(new BigDecimal("1000"))
                .userType(UserType.CUSTOMER)
                .balanceHistory(Collections.emptyList())
                .build();

        given(balanceRepository.findByMail(anyString())).willReturn(Optional.of(balance));

        var balanceAmount = balanceService.getBalance(anyString());

        assertThat(balanceAmount).isNotNull();
        assertThat(balanceAmount).isEqualTo(balance.getTotalCreditAmount());
        assertThat(balanceAmount).isNotEqualTo(new BigDecimal("0"));

    }

    @Test
    void test_getBalance_with_invalid_params() {
        given(balanceRepository.findByMail(anyString())).willThrow(PaymentNotFoundException.class);

        assertThatThrownBy(() -> balanceService.getBalance(anyString()))
                .isInstanceOf(PaymentNotFoundException.class);

    }

}
