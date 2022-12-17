package com.uber.api.common.api.exception;

public class PendingRequestNotFoundException extends RuntimeException {
    public PendingRequestNotFoundException(String message) {
        super(message);
    }

    public PendingRequestNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
