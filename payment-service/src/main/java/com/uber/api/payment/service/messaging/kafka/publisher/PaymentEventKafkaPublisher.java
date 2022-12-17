package com.uber.api.payment.service.messaging.kafka.publisher;

import com.uber.api.kafka.model.PaymentResponseAvroModel;
import com.uber.api.kafka.producer.KafkaMessageHelper;
import com.uber.api.kafka.producer.service.KafkaProducer;
import com.uber.api.outbox.OutboxStatus;
import com.uber.api.payment.service.config.PaymentServiceConfigData;
import com.uber.api.payment.service.dto.CallEventPayload;
import com.uber.api.payment.service.dto.CustomerOutboxMessage;
import com.uber.api.payment.service.messaging.helper.PaymentMessagingDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventKafkaPublisher implements PaymentResponseMessagePublisher {

    private final KafkaProducer<String , PaymentResponseAvroModel> kafkaProducer;
    private final PaymentServiceConfigData paymentServiceConfigData;

    private final KafkaMessageHelper kafkaMessageHelper;
    private final PaymentMessagingDataMapper paymentMessagingDataMapper;
    @Override
    public void publish(CustomerOutboxMessage message, BiConsumer<CustomerOutboxMessage, OutboxStatus> updateOutboxMessage) {
        CallEventPayload payload = kafkaMessageHelper.getEventOnPayload(message.getPayload(), CallEventPayload.class);
        var sagaId = message.getSagaId().toString();

        log.info("Publishing payment response for saga id: {}", sagaId);

        try {
            var paymentResponseAvroModel = paymentMessagingDataMapper.callEventPayloadToPaymentResponseAvroModel(sagaId,payload);

            kafkaProducer.send(paymentServiceConfigData.getPaymentResponseTopicName(),
                    sagaId, paymentResponseAvroModel,
                    kafkaMessageHelper.getKafkaCallback(
                            paymentServiceConfigData.getPaymentResponseTopicName(),
                            paymentResponseAvroModel,
                            message,
                            updateOutboxMessage,
                            payload.getRequestId(),
                            "PaymentResponseAvroModel"
                    ));
            log.info("PaymentResponseAvroModel sent to kafka for request id: {} and saga id: {}",
                    paymentResponseAvroModel.getRequestId(), sagaId);

        }
        catch (Exception e) {
            log.error("Error while publishing payment response for request id: {}", sagaId, e);
        }
    }


}
