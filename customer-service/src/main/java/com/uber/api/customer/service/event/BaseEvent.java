package com.uber.api.customer.service.event;

import com.uber.api.customer.service.entity.Customer;
import com.uber.api.common.api.entity.PendingRequest;
import com.uber.api.common.api.event.UberDomainEvent;

import java.time.ZonedDateTime;

public abstract class BaseEvent implements UberDomainEvent<Customer> {
    private final PendingRequest pendingRequest;

    private final ZonedDateTime createdAt;

    protected BaseEvent(PendingRequest pendingRequest, ZonedDateTime createdAt) {
        this.pendingRequest = pendingRequest;
        this.createdAt = createdAt;
    }

    public PendingRequest getPendingRequest() {
        return pendingRequest;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }


}
