package com.uber.api.customer.service.messaging.kafka.service.impl;

import com.uber.api.customer.service.dto.DriverCallResponse;
import com.uber.api.customer.service.messaging.kafka.service.DriverAcceptResponseMessageListener;
import com.uber.api.customer.service.saga.DriverSaga;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverAcceptResponseMessageListenerImpl implements DriverAcceptResponseMessageListener {

    private final DriverSaga driverSaga;

    @Override
    public void callApproved(DriverCallResponse driverApprovedResponseAvroModelToDriverApprovedResponse) {
        driverSaga.process(driverApprovedResponseAvroModelToDriverApprovedResponse);
        log.info("Customer Payment saga process completed id {}", driverApprovedResponseAvroModelToDriverApprovedResponse.getId());
    }

    @Override
    public void callRejected(DriverCallResponse driverApprovedResponseAvroModelToDriverApprovedResponse) {
        driverSaga.rollback(driverApprovedResponseAvroModelToDriverApprovedResponse);
        log.info("Customer Payment saga rollback completed id {}", driverApprovedResponseAvroModelToDriverApprovedResponse.getId());
    }
}
