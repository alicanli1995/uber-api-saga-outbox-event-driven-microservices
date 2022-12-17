package com.uber.api.customer.service.saga;

import com.uber.api.common.api.constants.CallStatus;
import com.uber.api.common.api.constants.CustomerStatus;
import com.uber.api.customer.service.entity.BalanceOutboxEntity;
import com.uber.api.customer.service.entity.Customer;
import com.uber.api.customer.service.entity.DriverApprovalOutbox;
import com.uber.api.customer.service.event.CustomerPaidEvent;
import com.uber.api.customer.service.dto.PaymentResponse;
import com.uber.api.customer.service.messaging.helper.CustomerMessagingDataMapper;
import com.uber.api.customer.service.outbox.helper.DriverOutboxHelper;
import com.uber.api.customer.service.repository.BalanceOutboxRepository;
import com.uber.api.customer.service.repository.CustomerRepository;
import com.uber.api.customer.service.repository.DriverApprovalOutboxRepository;
import com.uber.api.customer.service.saga.helper.CustomerSagaHelper;
import com.uber.api.customer.service.service.CustomerDomainService;
import com.uber.api.kafka.model.PaymentStatus;
import com.uber.api.outbox.OutboxStatus;
import com.uber.api.saga.SagaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.uber.api.outbox.SagaConst.CUSTOMER_PROCESSING_SAGA;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerPaymentSaga {
    private final DriverApprovalOutboxRepository driverApprovalOutboxRepository;
    private final BalanceOutboxRepository balanceOutboxRepository;
    private final CustomerRepository customerRepository;
    private final CustomerSagaHelper customerSagaHelper;
    private final CustomerMessagingDataMapper customerMessagingDataMapper;
    private final DriverOutboxHelper driverOutboxHelper;
    private final CustomerDomainService customerDomainService;


    public void process(PaymentResponse paymentResponse) {
        Optional<BalanceOutboxEntity> balanceOutboxEntityOptional = balanceOutboxRepository.findByTypeAndSagaIdAndSagaStatusIn(
                CUSTOMER_PROCESSING_SAGA, UUID.fromString(paymentResponse.getSagaId()), List.of(SagaStatus.COMPENSATING));

        if (balanceOutboxEntityOptional.isEmpty()) {
            log.info("Customer Payment saga process completed id {}", paymentResponse.getId());
            return;
        }

        BalanceOutboxEntity balanceOutboxEntity = balanceOutboxEntityOptional.get();
        CustomerPaidEvent customerPaidEvent = completePaymentForCustomer(paymentResponse);
        SagaStatus sagaStatus = customerSagaHelper.customerStatusToSagaStatus
                (customerPaidEvent.getPendingRequest().getCallStatus());

        balanceOutboxRepository.save(getUpdatedPaymentOutboxMessage(balanceOutboxEntity,sagaStatus));

        driverOutboxHelper.saveDriverOutboxMessage(customerMessagingDataMapper.
                        customerPaidEventToTaxiCallEventPayload(customerPaidEvent),
                sagaStatus,
                OutboxStatus.STARTED,
                UUID.fromString(paymentResponse.getSagaId()));

    }



    public void rollback(PaymentResponse paymentResponse) {
        Optional<BalanceOutboxEntity> balanceOutboxEntityOptional = balanceOutboxRepository.findByTypeAndSagaIdAndSagaStatusIn(
                CUSTOMER_PROCESSING_SAGA, UUID.fromString(paymentResponse.getSagaId()), List.of(SagaStatus.COMPENSATING));

        if (balanceOutboxEntityOptional.isEmpty()) {
            log.info("Customer Payment saga process is already completed id {}", paymentResponse.getId());
            return;
        }

        BalanceOutboxEntity balanceOutboxEntity = balanceOutboxEntityOptional.get();

        Customer customer =  customerRepository.findByEmail(paymentResponse.getCustomerMail()).get();

        SagaStatus sagaStatus = customerSagaHelper.customerStatusToSagaStatus
                (customer.getPendingRequest().getCallStatus());

        rollbackPaymentForTaxiCall(paymentResponse);


        balanceOutboxRepository.save(getUpdatedPaymentOutboxMessage(balanceOutboxEntity,sagaStatus));

        if (paymentResponse.getPaymentStatus().equals(PaymentStatus.CANCELED)){
            driverApprovalOutboxRepository.save(
                    getUpdatedApprovalOutboxMessage(paymentResponse.getSagaId(),
                            sagaStatus));
        }

        log.info("Customer Payment saga rollback completed id {}", paymentResponse.getId());
    }

    private DriverApprovalOutbox getUpdatedApprovalOutboxMessage(String sagaId, SagaStatus sagaStatus) {
        var orderApprovalOutboxMessageResponse = driverApprovalOutboxRepository.findByTypeAndSagaIdAndSagaStatus(
                CUSTOMER_PROCESSING_SAGA, UUID.fromString(sagaId), SagaStatus.COMPENSATING);

        if (orderApprovalOutboxMessageResponse.isEmpty()){
            throw new RuntimeException("Driver approval outbox message not found");
        }

        DriverApprovalOutbox driverApprovalOutbox = orderApprovalOutboxMessageResponse.get();

        driverApprovalOutbox.setSagaStatus(sagaStatus);
        driverApprovalOutbox.setProcessedAt(ZonedDateTime.now(ZoneId.of("UTC")));
        return driverApprovalOutbox;
    }

    private void rollbackPaymentForTaxiCall(PaymentResponse paymentResponse) {
        var customer = customerRepository.findByEmail(paymentResponse.getCustomerMail()).orElseThrow(
                () -> new RuntimeException("Customer not found"));
        customer.setCustomerStatus(CustomerStatus.AVAILABLE);
        customer.setPendingRequest(null);
        customerRepository.save(customer);
    }

    private CustomerPaidEvent completePaymentForCustomer(PaymentResponse paymentResponse) {
        Customer customer = customerRepository.findByEmail(paymentResponse.getCustomerMail()).orElseThrow(
                () -> new RuntimeException("Customer not found"));
        CustomerPaidEvent event = customerDomainService.payCustomer(customer, paymentResponse);
        customer.getPendingRequest().setCallStatus(CallStatus.DRIVER_WAITING);
        customerRepository.save(customer);
        return event;
    }


    private BalanceOutboxEntity getUpdatedPaymentOutboxMessage(BalanceOutboxEntity balanceOutboxEntity,
                                                               SagaStatus sagaStatus) {
        balanceOutboxEntity.setSagaStatus(sagaStatus);
        balanceOutboxEntity.setProcessedAt(ZonedDateTime.now(ZoneId.of("UTC")));
        return balanceOutboxEntity;
    }
}
