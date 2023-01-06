package com.uber.api.payment.service.repository;

import com.uber.api.payment.service.entity.Balance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
@DataJpaTest
class BalanceRepositoryTest {

    @Autowired
    private BalanceRepository balanceRepository;


    @Test
    void test_findByMail_with_invalid_params() {
        Optional<Balance> balance = balanceRepository.findByMail("test@gmail.com");

        assertThat(balance).isNotNull().isNotPresent();

    }

    @Test
    void test_findByMail_with_valid_params() {
        balanceRepository.save(Balance.builder()
                .mail("test@gmail.com")
                .id(UUID.randomUUID())
                .totalCreditAmount(new BigDecimal("1000"))
                .balanceHistory(Collections.emptyList())
                .build()
        );

        Optional<Balance> balance = balanceRepository.findByMail("test@gmail.com");

        assertThat(balance).isNotNull().isPresent();
        assertThat(balance.get().getMail()).isEqualTo("test@gmail.com");
        assertThat(balance.get().getTotalCreditAmount()).isEqualTo(new BigDecimal("1000"));
        assertThat(balance.get().getBalanceHistory()).isNotNull().isEmpty();
    }

}
