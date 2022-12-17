package com.uber.api.customer.service.helper;

import com.uber.api.common.api.constants.CustomerStatus;
import com.uber.api.common.api.entity.PendingRequest;
import com.uber.api.customer.service.entity.Customer;
import com.uber.api.customer.service.repository.BalanceOutboxRepository;
import com.uber.api.customer.service.repository.CustomerRepository;
import com.uber.api.customer.service.dto.CallDriverCommand;
import com.uber.api.customer.service.event.BaseDriverEvent;
import com.uber.api.customer.service.service.CustomerDomainService;
import com.uber.api.saga.SagaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerCallHelper {

    private final CustomerDomainService customerDomainService;
    private final CustomerRepository customerRepository;
    private final BalanceOutboxRepository balanceOutboxRepository;


    public BaseDriverEvent persistCall(CallDriverCommand callDriverCommand) {
        log.info("callDriverCommand: {}", callDriverCommand);
        var customer = checkCustomer(callDriverCommand.customerEmail());

        if (customer.isEmpty()){
            customer = Optional.of(createCustomer(callDriverCommand));
        }

        var driverEvent = customerDomainService.validateAndInitiateCallStatus
                (customer.get(),callDriverCommand.pendingRequest());

        saveCustomer(customer.get());

        log.info("Created Customer Event : {}", driverEvent);

        return driverEvent;
    }

    private Customer createCustomer(CallDriverCommand callDriverCommand) {
        return customerRepository.save(Customer.builder()
                        .name(callDriverCommand.pendingRequest().getCustomerEmail())
                        .ipAddress(callDriverCommand.pendingRequest().getIpAddress())
                        .email(callDriverCommand.customerEmail())
                        .customerStatus(CustomerStatus.AVAILABLE)
                .build());
    }

    private void saveCustomer(Customer customer) {
        customerRepository.save(customer);
    }

    private Optional<Customer> checkCustomer(String customerEmail) {
        return customerRepository.findByEmail(customerEmail);
    }

    public void updateCompletedCall(UUID requestId, String sagaId) {
        customerRepository.findByPendingRequest(PendingRequest.builder().requestId(requestId).build()).ifPresent(customer -> {
            customer.setCustomerStatus(CustomerStatus.AVAILABLE);
            customer.setPendingRequest(null);
            customerRepository.save(customer);

            balanceOutboxRepository.findBySagaId(UUID.fromString(sagaId)).ifPresent(balanceOutbox -> {
                balanceOutbox.setSagaStatus(SagaStatus.SUCCEEDED);
                balanceOutboxRepository.save(balanceOutbox);
            });
        });

    }
}
