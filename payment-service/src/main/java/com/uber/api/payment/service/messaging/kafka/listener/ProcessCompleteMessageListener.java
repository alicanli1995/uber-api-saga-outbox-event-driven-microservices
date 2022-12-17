package com.uber.api.payment.service.messaging.kafka.listener;


import com.uber.api.kafka.consumer.KafkaConsumer;
import com.uber.api.kafka.model.DriverCallRequestAvroModel;
import com.uber.api.payment.service.messaging.helper.PaymentMessagingDataMapper;
import com.uber.api.payment.service.messaging.service.ProcessCompletedMessageListener;
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

    private final PaymentMessagingDataMapper paymentMessagingDataMapper;
    private final ProcessCompletedMessageListener messageListener;


    @Override
    @KafkaListener(id = "${kafka-consumer-config.complete-consumer-group-id}",
            topics = "${payment-service.process-complete-topic-name}")
    public void receive(@Payload List<DriverCallRequestAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of process complete received with keys:{}, partitions:{} and offsets: {}",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString());


        messages.forEach(driverCallRequestAvroModel -> {
            log.info("Processing payment for request id: {}", driverCallRequestAvroModel.getRequestId());
            messageListener.processCompleteAndTransferMoneyDriver(paymentMessagingDataMapper
                    .driverCallRequestAvroModelToPaymentRequest(driverCallRequestAvroModel));
        });


    }
}
