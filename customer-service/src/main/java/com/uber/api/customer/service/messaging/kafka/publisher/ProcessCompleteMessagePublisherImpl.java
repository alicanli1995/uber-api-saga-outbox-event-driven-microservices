package com.uber.api.customer.service.messaging.kafka.publisher;

import com.uber.api.common.api.constants.WebSocketDataType;
import com.uber.api.common.api.dto.CallTaxiEventPayload;
import com.uber.api.common.api.event.UberWebSocketEvent;
import com.uber.api.customer.service.config.CustomerServiceConfigData;
import com.uber.api.customer.service.entity.DriverApprovalOutbox;
import com.uber.api.customer.service.event.ProcessCompleteEvent;
import com.uber.api.customer.service.helper.CustomerCallHelper;
import com.uber.api.customer.service.messaging.helper.CustomerMessagingDataMapper;
import com.uber.api.customer.service.messaging.kafka.service.ProcessCompleteMessagePublisher;
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
public class ProcessCompleteMessagePublisherImpl implements ProcessCompleteMessagePublisher {

    private final KafkaProducer<String , DriverCallRequestAvroModel> kafkaProducer;
    private final CustomerServiceConfigData customerServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;
    private final CustomerMessagingDataMapper customerMessagingDataMapper;
    private final CustomerCallHelper customerCallHelper;
    private final UberWebSocketEvent uberWebSocketEvent;


    @Override
    public void publish(DriverApprovalOutbox callOutboxMessage,
                        BiConsumer<DriverApprovalOutbox, OutboxStatus> outboxCallback) {

        CallTaxiEventPayload callTaxiEventPayload =
                kafkaMessageHelper.getEventOnPayload(callOutboxMessage.getPayload(),
                        CallTaxiEventPayload.class);

        String sagaId = callOutboxMessage.getSagaId().toString();

        customerCallHelper.updateCompletedCall(callTaxiEventPayload.requestId(),sagaId);

        try {
            DriverCallRequestAvroModel driverCallRequestAvroModel =
                    customerMessagingDataMapper
                            .callTaxiEventToDriverCallRequestAvroModel(sagaId,
                                    callTaxiEventPayload);

            kafkaProducer.send(customerServiceConfigData.getProcessCompleteTopicName(),
                    sagaId,
                    driverCallRequestAvroModel,
                    kafkaMessageHelper.getKafkaCallback(customerServiceConfigData.getProcessCompleteTopicName(),
                            driverCallRequestAvroModel,
                            callOutboxMessage,
                            outboxCallback,
                            callTaxiEventPayload.requestId().toString(),
                            "DriverCallRequestAvroModel"));

        } catch (Exception e) {
            log.error("Error while sending CallTaxiEventPayload to kafka for request id: {} and saga id: {}",
                    callTaxiEventPayload.requestId(),
                    sagaId,
                    e);
        }
        finally {
            publishWebSocketEvent(callTaxiEventPayload,WebSocketDataType.DRIVER);
            publishWebSocketEvent(callTaxiEventPayload,WebSocketDataType.CUSTOMER);
        }
    }

    private void publishWebSocketEvent(CallTaxiEventPayload callTaxiEventPayload, WebSocketDataType dataType) {
        uberWebSocketEvent.publishEvent(ProcessCompleteEvent
                .builder()
                        .dataType(dataType)
                        .customerMail(callTaxiEventPayload.customerEmail())
                        .driverMail(callTaxiEventPayload.driverEmail())
                        .requestId(String.valueOf(callTaxiEventPayload.requestId()))
                .build());
    }
}
