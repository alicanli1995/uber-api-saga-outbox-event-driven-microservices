package com.uber.api.customer.service.event;


import com.uber.api.common.api.constants.WebSocketDataType;
import com.uber.api.common.api.event.BusinessEvent;

public class DeclineDriverEvent extends BusinessEvent {

    private final String requestId;
    private final String driverStatus;
    private final WebSocketDataType dataType;


    public DeclineDriverEvent(String driverStatus, String requestId, WebSocketDataType dataType) {
        this.driverStatus = driverStatus;
        this.requestId = requestId;
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
