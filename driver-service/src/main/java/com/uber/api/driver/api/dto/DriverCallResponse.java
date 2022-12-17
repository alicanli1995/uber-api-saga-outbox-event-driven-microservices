package com.uber.api.driver.api.dto;

import lombok.Builder;

@Builder
public record DriverCallResponse(
        String requestId,
        String driverName,
        String driverPhone,
        String mail,
        String message
) {
}
