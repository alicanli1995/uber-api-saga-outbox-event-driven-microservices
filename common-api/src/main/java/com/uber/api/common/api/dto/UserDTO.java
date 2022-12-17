package com.uber.api.common.api.dto;


import lombok.Builder;

@Builder
public record UserDTO(
    String userName,
    String emailId,
    String password,
    String firstname,
    String lastName
) {
}
