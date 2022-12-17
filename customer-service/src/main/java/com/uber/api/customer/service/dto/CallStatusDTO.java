package com.uber.api.customer.service.dto;

import lombok.Builder;

@Builder
public record CallStatusDTO(
        String requestId,
        String sagaId,
        String driverName,
        String mail,
        String message
) { }




