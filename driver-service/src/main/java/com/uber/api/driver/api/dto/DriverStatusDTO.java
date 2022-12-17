package com.uber.api.driver.api.dto;

import lombok.Builder;

@Builder
public record DriverStatusDTO(
        String status,
        DriverRequestDTO requestDTO
) {
}
