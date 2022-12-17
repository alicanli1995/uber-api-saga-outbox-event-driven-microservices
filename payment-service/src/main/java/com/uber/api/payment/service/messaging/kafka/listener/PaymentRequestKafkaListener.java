package com.uber.api.payment.service.messaging.kafka.listener;

import com.uber.api.kafka.consumer.KafkaConsumer;
import com.uber.api.kafka.model.PaymentCustomerStatus;
import com.uber.api.kafka.model.PaymentRequestAvroModel;
import com.uber.api.payment.service.exception.KakfaDataAccessException;
import com.uber.api.payment.service.messaging.helper.PaymentMessagingDataMapper;
import com.uber.api.payment.service.messaging.service.PaymentRequestMessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLState;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestKafkaListener implements KafkaConsumer<PaymentRequestAvroModel> {

    private final PaymentMessagingDataMapper paymentMessagingDataMapper;
    private final PaymentRequestMessageListener paymentRequestMessageListener;

    @Override
    @KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}",
            topics = "${payment-service.payment-request-topic-name}")
    public void receive(@Payload List<PaymentRequestAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of payment requests received with keys:{}, partitions:{} and offsets: {}",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString());
        messages.forEach(paymentRequestAvroModel -> {
            try {
                if (Objects.equals(PaymentCustomerStatus.PENDING.name(),
                        paymentRequestAvroModel.getPaymentCustomerStatus().name())) {
                    log.info("Processing payment for request id: {}", paymentRequestAvroModel.getRequestId());
                    paymentRequestMessageListener.completePayment(paymentMessagingDataMapper
                            .paymentRequestAvroModelToPaymentRequest(paymentRequestAvroModel));
                }
                else if(PaymentCustomerStatus.CANCELED.name().equals
                        (paymentRequestAvroModel.getPaymentCustomerStatus().name())) {
                    log.info("Cancelling payment for request id: {}", paymentRequestAvroModel.getRequestId());
                    paymentRequestMessageListener.cancelPayment(paymentMessagingDataMapper
                            .paymentRequestAvroModelToPaymentRequest(paymentRequestAvroModel));
                }
            } catch (DataAccessException e) {
                SQLException sqlException = (SQLException) e.getRootCause();
                if (sqlException != null && sqlException.getSQLState() != null &&
                        PSQLState.UNIQUE_VIOLATION.getState().equals(sqlException.getSQLState())) {
                    //NO-OP for unique constraint exception
                    log.error("Caught unique constraint exception with sql state: {} " +
                                    "in PaymentRequestKafkaListener for request id: {}",
                            sqlException.getSQLState(), paymentRequestAvroModel.getRequestId());
                } else {
                    throw new KakfaDataAccessException("Throwing DataAccessException in" +
                            " PaymentRequestKafkaListener: " + e.getMessage(), e);
                }
            }
        });
    }
}
