package com.uber.api.driver.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.uber.api.common.api.constants.DriverStatus;
import com.uber.api.common.api.entity.Location;
import com.uber.api.common.api.entity.PendingRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverListDTO {
    private String id;
    private String name;
    private String email;
    private String phone;
    private DriverStatus driverStatus;
    private String ipAddress;
    private PendingRequest pendingRequest;
    @JsonProperty("driver_location")
    private Location locations;
    private BigDecimal price;
}
