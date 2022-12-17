package com.uber.api.payment.service.repository;

import com.uber.api.payment.service.entity.BalanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BalanceHistoryRepository extends JpaRepository<BalanceHistory, UUID> {
}