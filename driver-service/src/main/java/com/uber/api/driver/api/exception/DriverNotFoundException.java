package com.uber.api.driver.api.exception;

import javax.validation.constraints.Email;

public class DriverNotFoundException extends RuntimeException {

    public DriverNotFoundException(@Email String email) {
        super(String.format("Driver with email %s not found", email));
    }


}