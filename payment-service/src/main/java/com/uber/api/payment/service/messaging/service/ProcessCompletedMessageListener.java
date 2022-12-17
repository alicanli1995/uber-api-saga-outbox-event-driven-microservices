package com.uber.api.payment.service.messaging.service;

import com.uber.api.common.api.dto.CallTaxiEventPayload;

public interface ProcessCompletedMessageListener {
    void processCompleteAndTransferMoneyDriver(CallTaxiEventPayload driverCallRequestAvroModelToPaymentRequest);

}
