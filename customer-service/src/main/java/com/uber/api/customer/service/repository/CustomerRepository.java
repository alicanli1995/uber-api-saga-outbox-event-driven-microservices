package com.uber.api.customer.service.repository;

import com.uber.api.common.api.entity.PendingRequest;
import com.uber.api.customer.service.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByEmail(String customerEmail);

    Optional<Customer> findByPendingRequest(PendingRequest pendingRequest);

}
