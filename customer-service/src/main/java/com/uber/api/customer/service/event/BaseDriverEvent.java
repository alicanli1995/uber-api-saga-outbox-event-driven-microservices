package com.uber.api.customer.service.event;

import com.uber.api.common.api.entity.PendingRequest;

import java.time.ZonedDateTime;

public class BaseDriverEvent extends BaseEvent {
    public BaseDriverEvent(PendingRequest pendingRequest, ZonedDateTime createdAt) {
        super(pendingRequest, createdAt);
    }
}
