package com.uber.api.driver.api.dto;

import com.uber.api.kafka.model.DriverStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverCallRequestDTO {
    private String requestId;
    private String sagaId;
    private String ipAddress;
    private String driverEmail;
    private String customerMail;
    private BigDecimal price;
    private ZonedDateTime createdAt;
    private DriverStatus driverStatus;

}
