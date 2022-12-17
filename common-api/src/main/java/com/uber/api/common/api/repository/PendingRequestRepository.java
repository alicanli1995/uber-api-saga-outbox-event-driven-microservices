package com.uber.api.common.api.repository;

import com.uber.api.common.api.entity.PendingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PendingRequestRepository extends JpaRepository<PendingRequest, UUID> {
    Optional<PendingRequest> findByRequestId(UUID requestId);
}