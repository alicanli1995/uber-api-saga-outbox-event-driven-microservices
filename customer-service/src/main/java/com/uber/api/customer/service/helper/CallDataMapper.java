package com.uber.api.customer.service.helper;


import com.uber.api.customer.service.event.BaseDriverEvent;
import com.uber.api.customer.service.dto.TaxiPaymentEventPayload;
import com.uber.api.customer.service.event.CustomerPaymentCancelledEvent;
import com.uber.api.kafka.model.PaymentCustomerStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Component
public class CallDataMapper {
    public TaxiPaymentEventPayload callCreatedEventToBalanceEventPayload(BaseDriverEvent driverEvent) {
        return TaxiPaymentEventPayload.builder()
                .customerMail(driverEvent.getPendingRequest().getCustomerEmail())
                .paymentStatus(PaymentCustomerStatus.PENDING.toString())
                .createdAt(ZonedDateTime.now())
                .price(BigDecimal.valueOf(driverEvent.getPendingRequest().getOffer()))
                .requestId(String.valueOf(driverEvent.getPendingRequest().getRequestId()))
                .build();
    }

    public TaxiPaymentEventPayload orderCancelledEventToOrderPaymentEventPayload(CustomerPaymentCancelledEvent event) {
        return TaxiPaymentEventPayload.builder()
                .customerMail(event.getPendingRequest().getCustomerEmail())
                .paymentStatus(PaymentCustomerStatus.CANCELED.toString())
                .createdAt(ZonedDateTime.now())
                .price(BigDecimal.valueOf(event.getPendingRequest().getOffer()))
                .requestId(String.valueOf(event.getPendingRequest().getRequestId()))
                .build();
    }
}
