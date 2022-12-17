package com.uber.api.payment.service.exception;

public class KakfaDataAccessException extends RuntimeException {
    public KakfaDataAccessException(String message) {
        super(message);
    }

    public KakfaDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
