package com.uber.api.customer.service.service.impl;

import com.uber.api.common.api.constants.CallStatus;
import com.uber.api.common.api.constants.CustomerStatus;
import com.uber.api.common.api.constants.PaymentStatus;
import com.uber.api.common.api.entity.PendingRequest;
import com.uber.api.customer.service.entity.Customer;
import com.uber.api.customer.service.event.BaseDriverEvent;
import com.uber.api.customer.service.event.CustomerPaidEvent;
import com.uber.api.customer.service.dto.PaymentResponse;
import com.uber.api.customer.service.service.CustomerDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;


@Slf4j
@Component
@Validated
@RequiredArgsConstructor
public class CustomerDomainServiceImpl implements CustomerDomainService {
    @Override
    public BaseDriverEvent validateAndInitiateCallStatus(Customer customer, PendingRequest pendingRequest) {
        log.info("validateAndInitiateCallStatus");
        validateCustomerStatus(customer);
        initializePendingRequest(pendingRequest);
        initiateCallStatus(customer,pendingRequest);
        return new BaseDriverEvent(pendingRequest, ZonedDateTime.now());
    }

    private void initializePendingRequest(PendingRequest pendingRequest) {
        pendingRequest.setRequestId(UUID.randomUUID());
        pendingRequest.setCallStatus(CallStatus.PAYMENT_WAITING);
    }

    @Override
    public CustomerPaidEvent payCustomer(Customer customer, PaymentResponse paymentResponse) {
        if (!customer.getCustomerStatus().equals(CustomerStatus.WAITING))
            throw new RuntimeException("Customer is not in waiting state");
        customer.getPendingRequest().setPaymentStatus(PaymentStatus.COMPLETED);
        return new CustomerPaidEvent(customer.getPendingRequest(), ZonedDateTime.now(ZoneId.of("UTC")));
    }

    private void initiateCallStatus(Customer customer,PendingRequest pendingRequest) {
        customer.setCustomerStatus(CustomerStatus.WAITING);
        customer.setLocations(pendingRequest.getCustomerLocation());
        customer.setPendingRequest(pendingRequest);
        customer.setIpAddress(pendingRequest.getIpAddress());
    }

    private void validateCustomerStatus(Customer customer) {
        if (customer.getCustomerStatus().equals(CustomerStatus.WAITING) ||
                customer.getCustomerStatus().equals(CustomerStatus.ON_THE_WAY)) {
            throw new RuntimeException("Customer is blocked. Please contact customer support");
        }
    }



}
