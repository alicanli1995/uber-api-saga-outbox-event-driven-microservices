package com.uber.api.customer.service.service.impl;

import com.uber.api.common.api.constants.CustomerStatus;
import com.uber.api.common.api.dto.GeoIP;
import com.uber.api.common.api.entity.Location;
import com.uber.api.customer.service.client.DriverApiClient;
import com.uber.api.customer.service.client.LocationApi;
import com.uber.api.customer.service.dto.CustomerStatusDTO;
import com.uber.api.customer.service.dto.DriverDTO;
import com.uber.api.customer.service.entity.Customer;
import com.uber.api.customer.service.messaging.kafka.publisher.UserCreatedMessagePublisher;
import com.uber.api.customer.service.repository.CustomerRepository;
import com.uber.api.kafka.model.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerCallQueryHandler {
    private final CustomerRepository customerRepository;
    private final DriverApiClient driverApiClient;
    private final LocationApi locationApi;
    private final UserCreatedMessagePublisher userCreatedMessagePublisher;


    public CustomerStatusDTO getCustomerStatus(String mail, String name, String ip) {
        var customer = customerRepository.findByEmail(mail);
        if (customer.isPresent() ) {
            if (customer.get().getCustomerStatus().equals(CustomerStatus.WAITING) ||
                    customer.get().getCustomerStatus().equals(CustomerStatus.ON_THE_WAY)) {
                DriverDTO driverStatus;
                try {
                    driverStatus = driverApiClient.getDriverByMail(customer.get().getPendingRequest().getDriverEmail());
                }
                catch (Exception e){
                    throw new RuntimeException("Driver is not available", e);
                }
                return CustomerStatusDTO.builder()
                        .status(customer.get().getCustomerStatus().toString())
                        .driver(driverStatus)
                        .customerLocation(locationApi.getIpLocation(ip))
                        .build();
            }
            else {
                return CustomerStatusDTO.builder()
                        .status(customer.get().getCustomerStatus().toString())
                        .customerLocation(locationApi.getIpLocation(ip))
                        .build();
            }
        }
        else {
            Customer save = customerRepository.save(Customer.builder()
                        .email(mail)
                        .customerStatus(CustomerStatus.AVAILABLE)
                        .name(name)
                        .ipAddress(ip)
                        .locations(createLocation(ip))
                    .build());
            userCreatedMessagePublisher.publish(mail,
                    (s, s2) -> log.info("UserCreatedMessagePublisher sent to kafka for user email: {}", mail), UserType.CUSTOMER);
            return CustomerStatusDTO.builder()
                    .status(save.getCustomerStatus().toString())
                    .customerLocation(locationApi.getIpLocation(ip))
                    .build();
        }


    }

    private Location createLocation(String ip) {
        GeoIP ipLocation = locationApi.getIpLocation(ip);
        return Location.builder()
                .country(ipLocation.getFullLocation())
                .city(ipLocation.getCity())
                .distance(0.0)
                .latitude(ipLocation.getLatitude())
                .longitude(ipLocation.getLongitude())
                .build();
    }
}
