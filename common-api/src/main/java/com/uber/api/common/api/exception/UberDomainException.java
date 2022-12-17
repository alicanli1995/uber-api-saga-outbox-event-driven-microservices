package com.uber.api.common.api.exception;

public class UberDomainException extends DomainException{

        public UberDomainException(String message) {
            super(message);
        }

        public UberDomainException(String message, Throwable cause) {
            super(message, cause);
        }
}
