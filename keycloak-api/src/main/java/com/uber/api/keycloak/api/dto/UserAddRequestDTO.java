package com.uber.api.keycloak.api.dto;

import lombok.Builder;

@Builder
public record UserAddRequestDTO(
    String username,
    String email,
    String firstName,
    String lastName,
    String password
) {
}
