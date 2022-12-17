package com.uber.api.customer.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "customer-service")
public class CustomerServiceConfigData {
    private String paymentRequestTopicName;
    private String paymentResponseTopicName;
    private String driverCallRequestTopicName;
    private String driverCallResponseTopicName;
    private String processCompleteTopicName;
    private String customerCreatedTopicName;
}
