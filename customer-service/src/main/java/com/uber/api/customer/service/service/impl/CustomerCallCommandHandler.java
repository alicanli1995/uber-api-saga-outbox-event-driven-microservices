package com.uber.api.customer.service.service.impl;


import com.uber.api.customer.service.dto.CallDriverCommand;
import com.uber.api.customer.service.dto.CallStatusDTO;
import com.uber.api.customer.service.helper.CallDataMapper;
import com.uber.api.customer.service.helper.CustomerCallHelper;
import com.uber.api.customer.service.outbox.helper.RequestOutboxHelper;
import com.uber.api.customer.service.saga.helper.CustomerSagaHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerCallCommandHandler {
    private final CustomerCallHelper customerCallHelper;
    private final CallDataMapper callDataMapper;
    private final CustomerSagaHelper customerSagaHelper;
    private final RequestOutboxHelper requestOutboxHelper;

    @Transactional
    public CallStatusDTO callDriver(CallDriverCommand callDriverCommand) {
        var persistCall = customerCallHelper.persistCall(callDriverCommand);
        log.info("createDriverCall with id: {}", persistCall.getPendingRequest().getRequestId());

        UUID outboxMessage = requestOutboxHelper.savePaymentOutboxMessage(
                callDataMapper.callCreatedEventToBalanceEventPayload(persistCall),
                customerSagaHelper.customerStatusToSagaStatus(persistCall.getPendingRequest().getCallStatus()).toString(),
                UUID.randomUUID()
        );

        log.info("Returning DriverStatusDTO with request id : {}", persistCall.getPendingRequest().getRequestId());

        return CallStatusDTO.builder()
                .sagaId(String.valueOf(outboxMessage))
                .requestId(String.valueOf(persistCall.getPendingRequest().getRequestId()))
                .driverName(persistCall.getPendingRequest().getDriverEmail())
                .mail(persistCall.getPendingRequest().getCustomerEmail())
                .message("Call created")
                .build();
    }
}
