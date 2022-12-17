package com.uber.api.driver.api.event;

import com.uber.api.driver.api.entity.Driver;

public class CallApprovedEvent extends CallRequestApprovalEvent {

    public CallApprovedEvent(Driver driver) {
        super(driver);
    }

}
