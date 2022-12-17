package com.uber.api.payment.service.exception;


import com.uber.api.common.api.exception.DomainException;

public class PaymentNotFoundException extends DomainException {
    public PaymentNotFoundException(String message) {
        super(message);
    }

    public PaymentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
