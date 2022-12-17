package com.uber.api.payment.service.messaging.kafka.listener;

import com.uber.api.kafka.consumer.KafkaConsumer;
import com.uber.api.kafka.model.UserCreateRequest;
import com.uber.api.payment.service.messaging.helper.PaymentMessagingDataMapper;
import com.uber.api.payment.service.repository.BalanceRepository;
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
public class UserCreateMessageListener implements KafkaConsumer<UserCreateRequest> {

    private final PaymentMessagingDataMapper paymentMessagingDataMapper;

    private final BalanceRepository balanceRepository;

    @Override
    @KafkaListener(id = "${kafka-consumer-config.create-consumer-group-id}",
            topics = "${payment-service.user-created-topic-name}")
    public void receive(@Payload List<UserCreateRequest> messages,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of user create requests received with keys:{}, partitions:{} and offsets: {}",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString());

        messages.forEach(createRequest -> {
            if (balanceRepository.findByMail(createRequest.getUserMail()).isEmpty()) {
                var balanceEntity = paymentMessagingDataMapper.userCreateRequestToBalanceEntity(createRequest);
                balanceEntity.getBalanceHistory().forEach(balanceHistoryEntity -> balanceHistoryEntity.setBalance(balanceEntity));
                balanceRepository.save(balanceEntity);
                log.info("Saved balance entity for driver: {}", balanceEntity.getMail());
            } else {
                log.info("Balance entity already exists for driver: {}", createRequest.getUserMail());
            }
        });

    }
}
