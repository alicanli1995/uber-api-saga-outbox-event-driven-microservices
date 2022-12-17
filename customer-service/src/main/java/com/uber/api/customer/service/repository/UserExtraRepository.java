package com.uber.api.customer.service.repository;

import com.uber.api.customer.service.entity.UserExtra;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserExtraRepository extends JpaRepository<UserExtra, String> {
}