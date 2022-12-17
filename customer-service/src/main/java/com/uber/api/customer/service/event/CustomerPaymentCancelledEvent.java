package com.uber.api.customer.service.event;

import com.uber.api.common.api.entity.PendingRequest;

import java.time.ZonedDateTime;

public class CustomerPaymentCancelledEvent extends BaseEvent {

    public CustomerPaymentCancelledEvent(PendingRequest customer, ZonedDateTime createdAt) {
        super(customer, createdAt);
    }
}
