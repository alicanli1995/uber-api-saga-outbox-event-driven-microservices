package com.uber.api.customer.service.event;

import com.uber.api.common.api.entity.PendingRequest;

import java.time.ZonedDateTime;

public class CustomerPaidEvent extends BaseEvent {

    public CustomerPaidEvent(PendingRequest customer, ZonedDateTime createdAt) {
        super(customer, createdAt);
    }
}
