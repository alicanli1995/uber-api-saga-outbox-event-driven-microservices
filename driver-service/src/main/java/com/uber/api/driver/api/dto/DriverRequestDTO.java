package com.uber.api.driver.api.dto;

import com.uber.api.common.api.entity.Location;
import lombok.Builder;

@Builder
public record DriverRequestDTO(
        String requestId,
        String customerName,
        String customerEmail,
        Location customerLocation,
        Location customerDestination,
        Double distance,
        Double price,
        Boolean isSpecialOffer
        ) {
}
