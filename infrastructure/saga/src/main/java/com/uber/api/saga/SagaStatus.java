package com.uber.api.saga;

public enum SagaStatus {
    STARTED,
    FAILED,
    SUCCEEDED,
    PROCESSING,
    COMPENSATING,
    COMPENSATED
}
