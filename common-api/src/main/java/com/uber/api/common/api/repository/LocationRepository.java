package com.uber.api.common.api.repository;

import com.uber.api.common.api.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {
}