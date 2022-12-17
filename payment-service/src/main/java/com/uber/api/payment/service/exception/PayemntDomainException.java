package com.uber.api.payment.service.exception;


import com.uber.api.common.api.exception.DomainException;

public class PayemntDomainException extends DomainException {
    public PayemntDomainException(String message) {
        super(message);
    }

    public PayemntDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
