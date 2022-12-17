package com.uber.api.customer.service.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.uber.api.common.api.constants.CustomerStatus;
import com.uber.api.customer.service.dto.CallDriverCommand;
import com.uber.api.customer.service.dto.CallStatusDTO;
import com.uber.api.customer.service.helper.CallDataMapper;
import com.uber.api.customer.service.helper.CustomerCallHelper;
import com.uber.api.customer.service.outbox.helper.RequestOutboxHelper;
import com.uber.api.customer.service.saga.helper.CustomerSagaHelper;
import com.uber.api.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomerCallCommandHandler {
    private final CustomerCallHelper customerCallHelper;
    private final CallDataMapper callDataMapper;
    private final RequestOutboxHelper requestOutboxHelper;

    private final CustomerSagaHelper customerSagaHelper;

    @Transactional
    public CallStatusDTO callDriver(CallDriverCommand callDriverCommand) throws JsonProcessingException {
        var persistCall = customerCallHelper.persistCall(callDriverCommand);
        log.info("createDriverCall with id: {}", persistCall.getPendingRequest().getRequestId());

        UUID outboxMessage = requestOutboxHelper.savePaymentOutboxMessage(
                callDataMapper.callCreatedEventToBalanceEventPayload(persistCall),
                CustomerStatus.WAITING.toString(),
                customerSagaHelper.customerStatusToSagaStatus(callDriverCommand.pendingRequest().getCallStatus()).toString(),
                OutboxStatus.STARTED.toString(),
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
