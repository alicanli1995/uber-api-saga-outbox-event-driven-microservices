package com.uber.api.customer.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableFeignClients
@EnableJpaRepositories(basePackages = {"com.uber.api.customer.service" , "com.uber.api.common.api"}  )
@EntityScan(basePackages = {"com.uber.api.customer.service", "com.uber.api.common.api" })
@SpringBootApplication(scanBasePackages = "com.uber.api")
public class CustomerApi {
    public static void main(String[] args) {
        SpringApplication.run(CustomerApi.class, args);
    }
}
