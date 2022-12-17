package com.uber.api.payment.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = {"com.uber.api.payment.service.repository",  "com.uber.api.common.api"})
@EntityScan(basePackages = { "com.uber.api.payment.service", "com.uber.api.common.api" })
@SpringBootApplication(scanBasePackages = "com.uber.api")
public class PaymentService {
    public static void main(String[] args) {
        SpringApplication.run(PaymentService.class, args);
    }
}
