package com.uber.api.customer.service.service;


import com.uber.api.customer.service.entity.Customer;
import com.uber.api.common.api.entity.PendingRequest;
import com.uber.api.customer.service.event.BaseDriverEvent;
import com.uber.api.customer.service.event.CustomerPaidEvent;
import com.uber.api.customer.service.dto.PaymentResponse;

public interface CustomerDomainService {
    BaseDriverEvent validateAndInitiateCallStatus(Customer customer, PendingRequest pendingRequest);

    CustomerPaidEvent payCustomer(Customer customer, PaymentResponse paymentResponse);

}
