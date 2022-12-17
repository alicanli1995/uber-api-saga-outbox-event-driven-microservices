package com.uber.api.customer.service.messaging.kafka.publisher;

import com.uber.api.common.api.dto.CallTaxiEventPayload;
import com.uber.api.customer.service.config.CustomerServiceConfigData;
import com.uber.api.customer.service.entity.DriverApprovalOutbox;
import com.uber.api.customer.service.messaging.helper.CustomerMessagingDataMapper;
import com.uber.api.customer.service.messaging.kafka.service.CallDriverRequestMessagePublisher;
import com.uber.api.kafka.model.DriverCallRequestAvroModel;
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
public class CallDriverRequestEventKafkaPublisher implements CallDriverRequestMessagePublisher {

    private final KafkaProducer<String , DriverCallRequestAvroModel> kafkaProducer;
    private final CustomerServiceConfigData customerServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;
    private final CustomerMessagingDataMapper customerMessagingDataMapper;

    @Override
    public void publish(DriverApprovalOutbox callOutboxMessage,
                        BiConsumer<DriverApprovalOutbox, OutboxStatus> outboxCallback) {
        CallTaxiEventPayload callTaxiEventPayload =
                kafkaMessageHelper.getEventOnPayload(callOutboxMessage.getPayload(),
                        CallTaxiEventPayload.class);

        String sagaId = callOutboxMessage.getSagaId().toString();

        log.info("Received CallTaxiEventPayload for request id: {} and saga id: {}",
                callTaxiEventPayload.requestId(),
                sagaId);

        try {
            DriverCallRequestAvroModel driverCallRequestAvroModel =
                    customerMessagingDataMapper
                            .callTaxiEventToDriverCallRequestAvroModel(sagaId,
                                    callTaxiEventPayload);

            kafkaProducer.send(customerServiceConfigData.getDriverCallRequestTopicName(),
                    sagaId,
                    driverCallRequestAvroModel,
                    kafkaMessageHelper.getKafkaCallback(customerServiceConfigData.getDriverCallRequestTopicName(),
                            driverCallRequestAvroModel,
                            callOutboxMessage,
                            outboxCallback,
                            callTaxiEventPayload.requestId().toString(),
                            "DriverCallRequestAvroModel"));

            log.info("CallTaxiEventPayload sent to kafka for request id: {} and saga id: {}",
                    callTaxiEventPayload.requestId(),
                    sagaId);
        } catch (Exception e) {
            log.error("Error while sending CallTaxiEventPayload to kafka for request id: {} and saga id: {}",
                    callTaxiEventPayload.requestId(),
                    sagaId,
                    e);
        }

    }
}
