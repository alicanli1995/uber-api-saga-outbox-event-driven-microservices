package com.uber.api.customer.service.messaging.kafka.publisher;

import com.uber.api.customer.service.config.CustomerServiceConfigData;
import com.uber.api.kafka.model.UserCreateRequest;
import com.uber.api.kafka.model.UserType;
import com.uber.api.kafka.producer.service.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.function.BiConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreatedMessagePublisher {

    private final KafkaProducer<String , UserCreateRequest> kafkaProducer;
    private final CustomerServiceConfigData customerServiceConfigData;


    public void publish(String userMail, BiConsumer<String, String> callBack,UserType userType) {
        try {
            UserCreateRequest userCreateRequest = new UserCreateRequest();
            userCreateRequest.setCreatedAt(Instant.now());
            userCreateRequest.setUserMail(userMail);
            userCreateRequest.setOpeningBalance(BigDecimal.valueOf(10000L));
            userCreateRequest.setUserType(userType);
            kafkaProducer.send(customerServiceConfigData.getCustomerCreatedTopicName(),
                    userMail,
                    userCreateRequest, new ListenableFutureCallback<SendResult<String, UserCreateRequest>>() {
                        @Override
                        public void onFailure(Throwable ex) {
                            log.error("Error while sending UserCreateRequest message" +
                                    " for user mail: {}",
                                    userMail, ex);
                            callBack.accept(userMail, "FAILED");
                        }

                        @Override
                        public void onSuccess(SendResult<String, UserCreateRequest> result) {
                            log.info("UserCreateRequest sent to kafka for user mail: {}",
                                    userMail);
                            callBack.accept(userMail, "SUCCESS");
                        }
                    });

            log.info("UserCreatedMessagePublisher sent to kafka for user email: {}", userMail);

        } catch (Exception e) {
            log.error("Error while sending UserCreatedMessagePublisher message" +
                    " for user email: {}", userMail, e);
        }
    }

}
