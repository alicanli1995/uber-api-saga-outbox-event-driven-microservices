package com.uber.api.payment.service.messaging.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uber.api.common.api.constants.PaymentStatus;
import com.uber.api.common.api.constants.TransactionStatus;
import com.uber.api.common.api.constants.TransactionType;
import com.uber.api.common.api.dto.CallTaxiEventPayload;
import com.uber.api.kafka.model.*;
import com.uber.api.outbox.OutboxStatus;
import com.uber.api.payment.service.dto.CallEventPayload;
import com.uber.api.payment.service.dto.CustomerOutboxMessage;
import com.uber.api.payment.service.dto.PaymentRequest;
import com.uber.api.payment.service.entity.Balance;
import com.uber.api.payment.service.entity.BalanceHistory;
import com.uber.api.payment.service.entity.PaymentOutboxEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static com.uber.api.outbox.SagaConst.CUSTOMER_PROCESSING_SAGA;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentMessagingDataMapper {

    private final ObjectMapper objectMapper;

    public PaymentRequest paymentRequestAvroModelToPaymentRequest(PaymentRequestAvroModel paymentRequestAvroModel) {
        return PaymentRequest.builder()
                .id(paymentRequestAvroModel.getRequestId())
                .sagaId(paymentRequestAvroModel.getSagaId())
                .requestId(paymentRequestAvroModel.getRequestId())
                .customerMail(paymentRequestAvroModel.getCustomerMail())
                .price(paymentRequestAvroModel.getPrice())
                .createdAt(paymentRequestAvroModel.getCreatedAt())
                .status(PaymentCustomerStatus.valueOf(paymentRequestAvroModel.getPaymentCustomerStatus().name()))
                .build();

    }

    public CustomerOutboxMessage customerOutboxEntityToCustomerOutboxMessage(PaymentOutboxEntity paymentOutboxEntity) {
        return CustomerOutboxMessage.builder()
                .id(paymentOutboxEntity.getId())
                .sagaId(paymentOutboxEntity.getSagaId())
                .createdAt(paymentOutboxEntity.getCreatedAt())
                .type(paymentOutboxEntity.getType())
                .payload(paymentOutboxEntity.getPayload())
                .outboxStatus(paymentOutboxEntity.getOutboxStatus())
                .paymentStatus(paymentOutboxEntity.getPaymentStatus())
                .version(paymentOutboxEntity.getVersion())
                .build();

    }

    public PaymentOutboxEntity customerOutboxMessageToCustomerOutboxEntity(CustomerOutboxMessage customerOutboxMessage) {
        return PaymentOutboxEntity.builder()
                .id(customerOutboxMessage.getId())
                .sagaId(customerOutboxMessage.getSagaId())
                .createdAt(customerOutboxMessage.getCreatedAt())
                .type(customerOutboxMessage.getType())
                .payload(customerOutboxMessage.getPayload())
                .outboxStatus(customerOutboxMessage.getOutboxStatus())
                .paymentStatus(customerOutboxMessage.getPaymentStatus())
                .version(customerOutboxMessage.getVersion())
                .processedAt(ZonedDateTime.now(ZoneId.of("UTC")))
                .build();
    }

    public PaymentOutboxEntity paymentEventToCustomerOutboxEntity(CallEventPayload callEventPayload,
                                                                  PaymentStatus status,
                                                                  OutboxStatus started,
                                                                  UUID fromString) {
        return PaymentOutboxEntity.builder()
                .id(UUID.randomUUID())
                .sagaId(fromString)
                .createdAt(callEventPayload.getCreatedAt())
                .processedAt(ZonedDateTime.now(ZoneId.of("UTC")))
                .type(CUSTOMER_PROCESSING_SAGA)
                .payload(createPayload(callEventPayload))
                .paymentStatus(status)
                .outboxStatus(started)
                .build();
    }

    private String createPayload(CallEventPayload callEventPayload) {
        try {
            return objectMapper.writeValueAsString(callEventPayload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public  <T> T getEventOnPayload(String payload, Class<T> callEventPayloadClass) {
        try {
            return objectMapper.readValue(payload, callEventPayloadClass);
        } catch (JsonProcessingException e) {
            log.error("Could not read {} object!", callEventPayloadClass.getName(), e);
            throw new RuntimeException("Could not read " + callEventPayloadClass.getName() + " object!", e);
        }
    }


    public PaymentResponseAvroModel callEventPayloadToPaymentResponseAvroModel(String sagaId,
                                                                               CallEventPayload payload) {
        return PaymentResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId(sagaId)
                .setPaymentId(payload.getPaymentId())
                .setCustomerMail(payload.getCustomerMail())
                .setRequestId(payload.getRequestId())
                .setPrice(payload.getPrice())
                .setCreatedAt(Instant.from(payload.getCreatedAt()))
                .setPaymentStatus(com.uber.api.kafka.model.PaymentStatus.valueOf(payload.getPaymentStatus()))
                .setFailureMessages(payload.getFailureMessages())
                .build();

    }

    public Balance userCreateRequestToBalanceEntity(UserCreateRequest userCreateRequest) {
        return Balance.builder()
                .id(UUID.randomUUID())
                .mail(userCreateRequest.getUserMail())
                .userType(userCreateRequest.getUserType())
                .balanceHistory(List.of(
                                BalanceHistory.builder()
                                        .email(userCreateRequest.getUserMail())
                                        .transactionAmount(userCreateRequest.getOpeningBalance())
                                        .transactionStatus(TransactionStatus.ACCEPTED)
                                        .transactionType(TransactionType.DEBIT)
                                        .transactionDate(ZonedDateTime.now(ZoneId.of("UTC")))
                                        .build()))
                .totalCreditAmount(userCreateRequest.getOpeningBalance())
                .build();

    }

    public CallTaxiEventPayload driverCallRequestAvroModelToPaymentRequest(DriverCallRequestAvroModel driverCallRequestAvroModel) {
        return CallTaxiEventPayload.builder()
                .requestId(UUID.fromString(driverCallRequestAvroModel.getRequestId()))
                .driverEmail(driverCallRequestAvroModel.getDriverMail())
                .customerEmail(driverCallRequestAvroModel.getCustomerMail())
                .price(driverCallRequestAvroModel.getPrice().doubleValue())
                .build();
    }
}
