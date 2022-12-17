package com.uber.api.customer.service.event;

import com.uber.api.common.api.constants.WebSocketDataType;
import com.uber.api.common.api.event.BusinessEvent;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProcessCompleteEvent extends BusinessEvent {

    private String requestId;
    private String driverMail;
    private String customerMail;
    private WebSocketDataType dataType;

}
