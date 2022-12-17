package com.uber.api.customer.service.saga.helper;

import com.uber.api.common.api.constants.CallStatus;
import com.uber.api.saga.SagaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor

public class CustomerSagaHelper {


    public SagaStatus customerStatusToSagaStatus(CallStatus callStatus) {
        return switch (callStatus) {
            case PAYMENT_WAITING , DRIVER_WAITING-> SagaStatus.COMPENSATING;
            case ACCEPTED -> SagaStatus.COMPENSATED;
            case COMPLETED -> SagaStatus.SUCCEEDED;
            case DRIVER_REJECTED -> SagaStatus.PROCESSING;
            case PAYMENT_FAILED -> SagaStatus.FAILED;
            default -> SagaStatus.STARTED;
        };
    }
}
