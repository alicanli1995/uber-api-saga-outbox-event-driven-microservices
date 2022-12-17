package com.uber.api.customer.service.dto;

import com.uber.api.common.api.entity.PendingRequest;
import lombok.Builder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Builder
public record CallDriverCommand(
        @NotBlank String customerEmail,
        @NotBlank String ipAddress,
        @NotNull PendingRequest pendingRequest,
        @Email
        @NotBlank
        String driverEmail
) {
}
