package com.uber.api.common.api.dto;

import com.uber.api.common.api.entity.Location;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CallTaxiEventPayload(
        UUID requestId,
        String driverEmail,
        String customerEmail,
        Location customerLocation,
        Location customerDestination,
        Double distance,
        boolean isSpecialOffer,
        Double price
) {
}
