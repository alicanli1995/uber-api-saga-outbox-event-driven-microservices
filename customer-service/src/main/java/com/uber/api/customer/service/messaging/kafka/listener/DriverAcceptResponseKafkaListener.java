package com.uber.api.customer.service.messaging.kafka.listener;

import com.uber.api.common.api.constants.CallStatus;
import com.uber.api.common.api.constants.WebSocketDataType;
import com.uber.api.common.api.event.UberWebSocketEvent;
import com.uber.api.customer.service.event.AcceptDriverEvent;
import com.uber.api.customer.service.event.DeclineDriverEvent;
import com.uber.api.customer.service.messaging.helper.CustomerMessagingDataMapper;
import com.uber.api.customer.service.messaging.kafka.service.DriverAcceptResponseMessageListener;
import com.uber.api.kafka.consumer.KafkaConsumer;
import com.uber.api.kafka.model.DriverCallResponseAvroModel;
import com.uber.api.kafka.model.DriverStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverAcceptResponseKafkaListener implements KafkaConsumer<DriverCallResponseAvroModel> {

    private final CustomerMessagingDataMapper  customerMessagingDataMapper;

    private final DriverAcceptResponseMessageListener driverAcceptResponseMessageListener;

    private final UberWebSocketEvent uberWebSocketEvent;

    @Override
    @KafkaListener(
            id = "${kafka-consumer-config.driver-accept-consumer-group-id}",
            topics = "${customer-service.driver-call-response-topic-name}"
    )
    public void receive(@Payload List<DriverCallResponseAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offSets) {

        log.info("{} number of driver approval responses received with keys : {} , partitions : {} , offsets : {}",
                messages.size(), keys, partitions, offSets);

        messages.forEach(message -> {
            var callStatus = customerMessagingDataMapper.getCallStatus(message.getPendingRequestId());
            if (callStatus.equals(CallStatus.ACCEPTED)) {
                log.info("Processing successful taxi driver approval response for call id: {}", message.getPendingRequestId());
                driverAcceptResponseMessageListener.callApproved(customerMessagingDataMapper.
                        driverApprovedResponseAvroModelToDriverApprovedResponse(message));
                uberWebSocketEvent.publishEvent(new AcceptDriverEvent(
                        message.getPendingRequestId(),
                        message.getDriverStatus().toString(),
                        WebSocketDataType.DRIVER
                ));
            }
            else if (callStatus.equals(CallStatus.DRIVER_REJECTED)) {
                log.info("Processing unsuccessful taxi driver approval response for call id: {}", message.getPendingRequestId());
                uberWebSocketEvent.publishEvent(
                        new DeclineDriverEvent(
                                DriverStatus.AVAILABLE.toString(),
                                message.getPendingRequestId(),
                                WebSocketDataType.DRIVER
                        )
                );
                driverAcceptResponseMessageListener.callRejected(customerMessagingDataMapper.
                        driverApprovedResponseAvroModelToDriverApprovedResponse(message));
            }
        });
    }
}
