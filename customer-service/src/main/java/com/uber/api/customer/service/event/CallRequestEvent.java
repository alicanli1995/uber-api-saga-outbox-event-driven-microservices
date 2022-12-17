package com.uber.api.customer.service.event;

import com.uber.api.common.api.constants.WebSocketDataType;
import com.uber.api.common.api.dto.CallTaxiEventPayload;
import com.uber.api.common.api.event.BusinessEvent;

public class CallRequestEvent extends BusinessEvent {

        private final CallTaxiEventPayload callDTO;
        private final String driverMail;
        private final WebSocketDataType dataType;


        public CallRequestEvent(CallTaxiEventPayload callDTO, String driverMail, WebSocketDataType dataType) {
                this.callDTO = callDTO;
                this.driverMail = driverMail;
            this.dataType = dataType;
        }

        public CallTaxiEventPayload getCallDTO() {
            return callDTO;
        }

        public String getDriverMail() {
            return driverMail;
        }

        public WebSocketDataType getDataType() {
            return dataType;
        }


}
