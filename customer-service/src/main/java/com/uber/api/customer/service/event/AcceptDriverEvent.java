package com.uber.api.customer.service.event;


import com.uber.api.common.api.constants.WebSocketDataType;
import com.uber.api.common.api.event.BusinessEvent;

public class AcceptDriverEvent extends BusinessEvent {

    private final String requestId;
    private final String driverStatus;
    private final WebSocketDataType dataType;

    public AcceptDriverEvent(String requestId, String driverStatus, WebSocketDataType dataType) {
        this.requestId = requestId;
        this.driverStatus = driverStatus;
        this.dataType = dataType;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getDriverStatus() {
        return driverStatus;
    }

    public WebSocketDataType getDataType() {
        return dataType;
    }



}
