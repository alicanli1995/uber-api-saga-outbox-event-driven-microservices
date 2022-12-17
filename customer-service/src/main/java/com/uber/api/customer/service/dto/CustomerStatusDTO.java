package com.uber.api.customer.service.dto;

import com.uber.api.common.api.dto.GeoIP;
import lombok.Builder;

@Builder
public record CustomerStatusDTO(
        String status,
        GeoIP customerLocation,
        DriverDTO driver
) {
}
