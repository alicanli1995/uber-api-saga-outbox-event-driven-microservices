package com.uber.api.driver.api.messaging.kafka.publisher;

import com.uber.api.driver.api.config.DriverServiceConfigData;
import com.uber.api.driver.api.dto.CallApprovedEventPayload;
import com.uber.api.driver.api.entity.CustomerRequestOutboxEntity;
import com.uber.api.driver.api.helper.DriverApiHelper;
import com.uber.api.kafka.model.DriverCallResponseAvroModel;
import com.uber.api.kafka.producer.KafkaMessageHelper;
import com.uber.api.kafka.producer.service.KafkaProducer;
import com.uber.api.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerAcceptResponseMessagePublisher {

    private final DriverApiHelper driverApiHelper;
    private final KafkaProducer<String, DriverCallResponseAvroModel> kafkaProducer;
    private final DriverServiceConfigData driverServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;

    public void publish(CustomerRequestOutboxEntity customerRequestOutboxEntity,
                        BiConsumer<CustomerRequestOutboxEntity, OutboxStatus> callBack) {
        var payload = kafkaMessageHelper.getEventOnPayload(customerRequestOutboxEntity.getPayload(),
                CallApprovedEventPayload.class);

        String sagaId = customerRequestOutboxEntity.getSagaId().toString();

        try {
            DriverCallResponseAvroModel driverCallResponseAvroModel =
                    driverApiHelper.getDriverCallResponseAvroModel(sagaId, payload);

            kafkaProducer.send(driverServiceConfigData.getDriverCallResponseTopicName(),
                    sagaId,
                    driverCallResponseAvroModel,
                    kafkaMessageHelper.getKafkaCallback(driverServiceConfigData
                                    .getDriverCallResponseTopicName(),
                            driverCallResponseAvroModel,
                            customerRequestOutboxEntity,
                            callBack,
                            payload.getCustomerEmail(),
                            "DriverCallResponseAvroModel"));

            log.info("DriverCallResponseAvroModel sent to kafka for request id: {} and saga id: {}",
                    driverCallResponseAvroModel.getPendingRequestId(), sagaId);
        } catch (Exception e) {
            log.error("Error while sending DriverCallResponseAvroModel message" +
                    " for request id: {} and saga id: {}",
                    payload.getCustomerEmail(), sagaId, e);
        }

    }
}
