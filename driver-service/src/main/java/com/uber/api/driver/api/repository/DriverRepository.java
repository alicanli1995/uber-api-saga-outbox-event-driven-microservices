package com.uber.api.driver.api.repository;

import com.uber.api.common.api.entity.PendingRequest;
import com.uber.api.driver.api.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, String> {
    Optional<Driver> findByPendingRequest(PendingRequest pendingRequest);
    Optional<Driver> findByEmail(String email);

    List<Driver> findAllByIpAddress(String ipAddress);

    @Query("select (count(d) > 0) from Driver d where d.ipAddress = ?1")
    boolean existsByIpAddress(String ipAddress);

}