package com.uber.api.customer.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserExtraNotFoundException extends RuntimeException {

    public UserExtraNotFoundException(String username) {
        super(String.format("UserExtra of %s not found", username));
    }
}
