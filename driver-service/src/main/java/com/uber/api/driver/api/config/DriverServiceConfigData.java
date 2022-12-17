package com.uber.api.driver.api.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "driver-service")
public class DriverServiceConfigData {

    private String driverCallRequestTopicName;
    private String driverCallResponseTopicName;

    private String driverCreateTopicName;

}
