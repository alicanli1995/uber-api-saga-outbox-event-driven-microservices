package com.uber.api.customer.service.messaging.kafka.service;

import com.uber.api.customer.service.dto.DriverCallResponse;

public interface DriverAcceptResponseMessageListener {
    void callApproved(DriverCallResponse driverApprovedResponseAvroModelToDriverApprovedResponse);

    void callRejected(DriverCallResponse driverApprovedResponseAvroModelToDriverApprovedResponse);

}
