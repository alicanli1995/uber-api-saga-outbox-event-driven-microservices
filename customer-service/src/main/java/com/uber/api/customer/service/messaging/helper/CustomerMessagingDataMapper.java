package com.uber.api.customer.service.messaging.helper;

import com.uber.api.common.api.constants.CallStatus;
import com.uber.api.common.api.dto.CallTaxiEventPayload;
import com.uber.api.common.api.entity.PendingRequest;
import com.uber.api.common.api.repository.PendingRequestRepository;
import com.uber.api.customer.service.dto.DriverCallResponse;
import com.uber.api.customer.service.dto.PaymentResponse;
import com.uber.api.customer.service.dto.TaxiPaymentEventPayload;
import com.uber.api.customer.service.event.CustomerPaidEvent;
import com.uber.api.kafka.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerMessagingDataMapper {

    private final PendingRequestRepository pendingRequestRepository;

    public PaymentRequestAvroModel taxiPaymentEventToPaymentRequestAvroModel(String sagaId, TaxiPaymentEventPayload taxiPaymentEventPayload) {

        return PaymentRequestAvroModel.newBuilder()
                .setSagaId(sagaId)
                .setCustomerMail(taxiPaymentEventPayload.getCustomerMail())
                .setPrice(taxiPaymentEventPayload.getPrice())
                .setRequestId(taxiPaymentEventPayload.getRequestId())
                .setPaymentCustomerStatus(PaymentCustomerStatus.valueOf(taxiPaymentEventPayload.getPaymentStatus()))
                .setCreatedAt(taxiPaymentEventPayload.getCreatedAt().toInstant())
                .build();
    }


    public PaymentResponse paymentResponseAvroModelToPaymentResponse(PaymentResponseAvroModel paymentResponseAvroModel) {
        return PaymentResponse.builder()
                .id(paymentResponseAvroModel.getId())
                .sagaId(paymentResponseAvroModel.getSagaId())
                .requestId(paymentResponseAvroModel.getRequestId())
                .paymentId(paymentResponseAvroModel.getPaymentId())
                .customerMail(paymentResponseAvroModel.getCustomerMail())
                .price(paymentResponseAvroModel.getPrice())
                .createdAt(paymentResponseAvroModel.getCreatedAt())
                .paymentStatus(PaymentStatus.valueOf(paymentResponseAvroModel.getPaymentStatus().toString()))
                .failureMessages(paymentResponseAvroModel.getFailureMessages())
                .build();
    }

    public CallTaxiEventPayload customerPaidEventToTaxiCallEventPayload(CustomerPaidEvent customerPaidEvent) {
        return CallTaxiEventPayload.builder()
                .distance(customerPaidEvent.getPendingRequest().getCustomerLocation().getDistance())
                .customerDestination(customerPaidEvent.getPendingRequest().getCustomerDestination())
                .customerEmail(customerPaidEvent.getPendingRequest().getCustomerEmail())
                .customerLocation(customerPaidEvent.getPendingRequest().getCustomerLocation())
                .price(customerPaidEvent.getPendingRequest().getOffer())
                .driverEmail(customerPaidEvent.getPendingRequest().getDriverEmail())
                .requestId(customerPaidEvent.getPendingRequest().getRequestId())
                .build();
    }

    public DriverCallRequestAvroModel callTaxiEventToDriverCallRequestAvroModel(String sagaId, CallTaxiEventPayload callTaxiEventPayload) {
        return DriverCallRequestAvroModel.newBuilder()
                .setSagaId(sagaId)
                .setRequestId(String.valueOf(callTaxiEventPayload.requestId()))
                .setCustomerMail(callTaxiEventPayload.customerEmail())
                .setDriverMail(callTaxiEventPayload.driverEmail())
                .setPrice(BigDecimal.valueOf(callTaxiEventPayload.price()))
                .setCreatedAt(ZonedDateTime.now().toInstant())
                .setDriverStatus(DriverStatus.AVAILABLE)
                .setIpAddress(findIpAddress(callTaxiEventPayload.requestId()))
                .build();
    }

    private String findIpAddress (UUID requestId) {
        Optional<PendingRequest> request = pendingRequestRepository.findByRequestId(requestId);
        return request.map(PendingRequest::getIpAddress).orElseThrow(
                () -> new RuntimeException("No pending request found for request id: " + requestId));
    }

    public CallStatus getCallStatus(String pendingRequestId) {
        Optional<PendingRequest> pendingRequest = pendingRequestRepository.findByRequestId(UUID.fromString(pendingRequestId));
        return pendingRequest.map(PendingRequest::getCallStatus).orElseThrow(
                () -> new RuntimeException("No pending request found for request id: " + pendingRequestId));
    }

    public DriverCallResponse driverApprovedResponseAvroModelToDriverApprovedResponse(DriverCallResponseAvroModel message) {
        return DriverCallResponse.builder()
                .id(message.getId())
                .sagaId(message.getSagaId())
                .pendingRequestId(message.getPendingRequestId())
                .driverMail(message.getDriverMail())
                .driverStatus(DriverStatus.valueOf(message.getDriverStatus().toString()))
                .failureMessages(message.getFailureMessages())
                .createdAt(message.getCreatedAt())
                .build();
    }

    public CallTaxiEventPayload createCallTaxiEventPayload(PaymentResponseAvroModel paymentResponseAvroModel) {
        var pendingRequest = pendingRequestRepository.findByRequestId(UUID.fromString(paymentResponseAvroModel.getRequestId())).orElseThrow(
                () -> new RuntimeException("No pending request found for request id: " + paymentResponseAvroModel.getRequestId()));
        return CallTaxiEventPayload.builder()
                .requestId(pendingRequest.getRequestId())
                .customerEmail(paymentResponseAvroModel.getCustomerMail())
                .driverEmail(pendingRequest.getDriverEmail())
                .distance(pendingRequest.getCustomerLocation().getDistance())
                .customerDestination(pendingRequest.getCustomerDestination())
                .customerLocation(pendingRequest.getCustomerLocation())
                .price(paymentResponseAvroModel.getPrice().doubleValue())
                .isSpecialOffer(pendingRequest.getIsSpecialOffer())
                .build();
    }
}
