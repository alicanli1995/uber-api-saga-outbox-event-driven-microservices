package com.uber.api.driver.api.messaging.kafka.listener;

import com.uber.api.driver.api.messaging.helper.DriverRequestKafkaHelper;
import com.uber.api.driver.api.service.CallDriverRequestService;
import com.uber.api.kafka.consumer.KafkaConsumer;
import com.uber.api.kafka.model.DriverCallRequestAvroModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessCompleteMessageListener implements KafkaConsumer<DriverCallRequestAvroModel> {

    private final CallDriverRequestService callDriverRequestService;
    private final DriverRequestKafkaHelper driverRequestKafkaHelper;

    @Override
    @KafkaListener(id = "${kafka-consumer-config.process-completed-consumer-group-id}",
            topics = "${driver-service.process-completed-topic-name}")
    public void receive(@Payload List<DriverCallRequestAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {

        log.info("{} number of process completed requests received with keys {}, partitions {} and offsets {}" +
                        ", updating driver status",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString());

        messages.forEach(callRequestAvroModel -> {
            log.info("Processing process completed for request id: {}", callRequestAvroModel.getRequestId());
            callDriverRequestService.processCompleted(driverRequestKafkaHelper.
                    driverCallRequestAvroModelToCallDriverDTO(callRequestAvroModel));

        });
    }
}
