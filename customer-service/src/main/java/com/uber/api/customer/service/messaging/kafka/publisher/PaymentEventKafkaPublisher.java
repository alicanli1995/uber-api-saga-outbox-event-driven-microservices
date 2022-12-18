package com.uber.api.customer.service.messaging.kafka.publisher;

import com.uber.api.customer.service.config.CustomerServiceConfigData;
import com.uber.api.customer.service.dto.TaxiPaymentEventPayload;
import com.uber.api.customer.service.messaging.helper.CustomerMessagingDataMapper;
import com.uber.api.customer.service.messaging.kafka.service.PaymentRequestMessagePublisher;
import com.uber.api.customer.service.dto.TaxiPaymentOutboxMessage;
import com.uber.api.kafka.model.PaymentRequestAvroModel;
import com.uber.api.kafka.producer.KafkaMessageHelper;
import com.uber.api.kafka.producer.service.KafkaProducer;
import com.uber.api.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;


@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventKafkaPublisher implements PaymentRequestMessagePublisher {

    private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;
    private final CustomerServiceConfigData customerServiceConfigData;
    private final CustomerMessagingDataMapper customerMessagingDataMapper;
    private final KafkaMessageHelper kafkaMessageHelper;

    @Override
    public void publish(TaxiPaymentOutboxMessage message, BiConsumer<TaxiPaymentOutboxMessage, OutboxStatus> callback) {
        TaxiPaymentEventPayload taxiPaymentEventPayload =
                kafkaMessageHelper.getEventOnPayload(message.getPayload() , TaxiPaymentEventPayload.class);

        String sagaId = message.getSagaId().toString();

        log.info("Received TaxiPaymentOutboxMessage for taxi id: {} and saga id: {}",
                taxiPaymentEventPayload.getCustomerMail(),
                sagaId);

        try {
            PaymentRequestAvroModel paymentRequestAvroModel = customerMessagingDataMapper
                    .taxiPaymentEventToPaymentRequestAvroModel(sagaId, taxiPaymentEventPayload);

            kafkaProducer.send(customerServiceConfigData.getPaymentRequestTopicName(),
                    sagaId,
                    paymentRequestAvroModel,
                    kafkaMessageHelper.getKafkaCallback(customerServiceConfigData.getPaymentRequestTopicName(),
                            paymentRequestAvroModel,
                            message,
                            callback,
                            taxiPaymentEventPayload.getCustomerMail(),
                            "PaymentRequestAvroModel"));

            log.info("TaxiPaymentEventPayload sent to Kafka for taxi id: {} and saga id: {}",
                    taxiPaymentEventPayload.getCustomerMail(), sagaId);
        } catch (Exception e) {
            log.error("Error while sending TaxiPaymentEventPayload" +
                            " to kafka with taxi id: {} and saga id: {}, error: {}",
                    taxiPaymentEventPayload.getCustomerMail(), sagaId, e.getMessage());
        }


    }
}
