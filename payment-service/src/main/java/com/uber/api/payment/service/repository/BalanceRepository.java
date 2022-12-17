package com.uber.api.payment.service.repository;

import com.uber.api.payment.service.entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;
import java.util.UUID;

@EnableJpaRepositories
public interface BalanceRepository extends JpaRepository<Balance, UUID> {
    Optional<Balance> findByMail(String customerMail);

}
