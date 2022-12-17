package com.uber.api.kafka.producer.service.impl;

import com.uber.api.kafka.producer.service.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.PreDestroy;
import java.io.Serializable;
import java.util.Objects;


@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducerImpl<K extends Serializable, V extends SpecificRecordBase> implements KafkaProducer<K, V> {

    private final KafkaTemplate<K, V> kafkaTemplate;

    @Override
    public void send(String topicName, K key, V message, ListenableFutureCallback<SendResult<K, V>> callback) {
        log.info("Sending message to topic: {}, also message {}", topicName,message);
        kafkaTemplate.send(topicName, key, message)
                .addCallback(new ListenableFutureCallback<>() {
                    @Override
                    public void onFailure(Throwable ex) {
                        log.error("Error sending message to topic: {}, also message {}", topicName, message, ex);
                        callback.onFailure(ex);
                    }

                    @Override
                    public void onSuccess(SendResult<K, V> result) {
                        log.info("Message sent to topic: {}, also message {}", topicName, message);
                        callback.onSuccess(result);
                    }
                });
    }

    @PreDestroy
    public void destroy() {
        log.info("KafkaProducerImpl is being destroyed");
        if (Objects.nonNull(kafkaTemplate)) {
            kafkaTemplate.destroy();
        }
    }


}
