package com.uber.api.common.api.event;

public interface UberWebSocketEvent {
    void publishEvent(BusinessEvent event);

}
