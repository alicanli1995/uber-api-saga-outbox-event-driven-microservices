package com.uber.api.driver.api.messaging.helper;

import com.uber.api.driver.api.dto.DriverCallRequestDTO;
import com.uber.api.kafka.model.DriverCallRequestAvroModel;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class DriverRequestKafkaHelper {
    public DriverCallRequestDTO driverCallRequestAvroModelToCallDriverDTO(DriverCallRequestAvroModel callRequestAvroModel) {
        return DriverCallRequestDTO.builder()
                .sagaId(callRequestAvroModel.getSagaId())
                .requestId(callRequestAvroModel.getRequestId())
                .driverStatus(callRequestAvroModel.getDriverStatus())
                .ipAddress(callRequestAvroModel.getIpAddress())
                .price(callRequestAvroModel.getPrice())
                .customerMail(callRequestAvroModel.getCustomerMail())
                .driverEmail(callRequestAvroModel.getDriverMail())
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                .build();
    }
}
