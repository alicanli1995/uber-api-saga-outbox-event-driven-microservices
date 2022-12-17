package com.uber.api.driver.api.event;

import com.uber.api.common.api.event.UberDomainEvent;
import com.uber.api.driver.api.entity.Driver;

public abstract class CallRequestApprovalEvent implements UberDomainEvent<Driver> {

        private final Driver driver;

        public CallRequestApprovalEvent(Driver driver) {
            this.driver = driver;
        }

        public Driver getDriver() {
            return driver;
        }


}
