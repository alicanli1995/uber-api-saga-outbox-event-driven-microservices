package com.uber.api.driver.api.helper;

import com.github.javafaker.Faker;
import com.uber.api.common.api.constants.DriverStatus;
import com.uber.api.common.api.entity.Location;
import com.uber.api.common.api.entity.PendingRequest;
import com.uber.api.common.api.exception.PendingRequestNotFoundException;
import com.uber.api.common.api.repository.PendingRequestRepository;
import com.uber.api.driver.api.client.LocationApi;
import com.uber.api.driver.api.dto.CallApprovedEventPayload;
import com.uber.api.driver.api.dto.DriverCallRequestDTO;
import com.uber.api.driver.api.dto.DriverListDTO;
import com.uber.api.driver.api.dto.GeoIP;
import com.uber.api.driver.api.entity.CustomerRequestOutboxEntity;
import com.uber.api.driver.api.entity.Driver;
import com.uber.api.driver.api.event.CallApprovedEvent;
import com.uber.api.driver.api.event.CallRequestApprovalEvent;
import com.uber.api.driver.api.exception.DriverNotFoundException;
import com.uber.api.driver.api.messaging.kafka.publisher.UserCreatedMessagePublisher;
import com.uber.api.driver.api.repository.CustomerRequestOutboxEntityRepository;
import com.uber.api.driver.api.repository.DriverRepository;
import com.uber.api.kafka.model.DriverCallResponseAvroModel;
import com.uber.api.kafka.model.UserType;
import com.uber.api.keycloak.api.dto.UserAddRequestDTO;
import com.uber.api.keycloak.api.service.KeycloakService;
import com.uber.api.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static com.uber.api.common.api.constants.CONSTANTS.*;
import static com.uber.api.driver.api.security.WebSecurityConfig.UBER_DRIVER;
import static com.uber.api.outbox.SagaConst.CUSTOMER_PROCESSING_SAGA;

@Slf4j
@Component
@RequiredArgsConstructor
public class DriverApiHelper {
    private final DriverRepository driverRepository;
    private final KeycloakService keycloakService;
    private final LocationApi geoIPLocationService;
    private final CustomerRequestOutboxEntityRepository customerRequestOutboxEntityRepository;
    private final PendingRequestRepository pendingRequestRepository;
    private final UserCreatedMessagePublisher userCreatedMessagePublisher;

    public Driver findDriver(DriverCallRequestDTO driverCallRequestAvroModelToCallDriverDTO) {
        var pending = pendingRequestRepository
                .findByRequestId(UUID.fromString(driverCallRequestAvroModelToCallDriverDTO.getRequestId())).orElseThrow(
                        () -> new PendingRequestNotFoundException("Pending request not found for request id: " + driverCallRequestAvroModelToCallDriverDTO.getRequestId()));
        return driverRepository.findByEmail(pending.getDriverEmail()).orElseThrow(
                () -> new DriverNotFoundException("Driver not found for email: " + pending.getDriverEmail()));
    }

    public boolean publishIfOutboxMessageProcessed(DriverCallRequestDTO driverCallRequestAvroModelToCallDriverDTO) {
        Optional<CustomerRequestOutboxEntity> byTypeAndSagaIdAndOutboxStatus = customerRequestOutboxEntityRepository.findByTypeAndSagaIdAndOutboxStatus(
                CUSTOMER_PROCESSING_SAGA,
                UUID.fromString(driverCallRequestAvroModelToCallDriverDTO.getSagaId()),
                OutboxStatus.COMPLETED);
        return byTypeAndSagaIdAndOutboxStatus.isPresent();
    }


    public List<DriverListDTO> generateDummyDriverList(GeoIP geoIP, Double distance) {
        if ((   !driverRepository.existsByIpAddress(geoIP.getIpAddress()) &&
                nearDriverList(geoIP.getLatitude(), geoIP.getLongitude(),distance).isEmpty()) ||
                nearDriverList(geoIP.getLatitude(), geoIP.getLongitude(),distance).isEmpty() ) {
            List<Driver> driverList = new ArrayList<>();
            Stream.iterate(0, i -> i + 1)
                    .limit(2)
                    .forEach(i -> {
                        Map<String,Double[]> locationList = geoIPLocationService.randomDriverLocationGenerator(
                                3,geoIP.getLatitude(), geoIP.getLongitude());
                        GeoIP geoInfoResponse;
                        Faker faker = new Faker();

                        geoInfoResponse = geoIPLocationService.getIpLocation(geoIP.getIpAddress());


                        Driver save = driverRepository.save(Driver.builder()
                                .ipAddress(geoIP.getIpAddress())
                                .name(faker.name().fullName())
                                .phone(faker.phoneNumber().cellPhone())
                                .email(faker.internet().emailAddress())
                                .driverStatus(DriverStatus.AVAILABLE)
                                .locations(
                                        Location.builder()
                                                .latitude(locationList.get(LOCATION + i)[0])
                                                .longitude(locationList.get(LOCATION + i)[1])
                                                .distance(locationList.get(LOCATION + i)[2])
                                                .country(geoInfoResponse.getFullLocation().split(COMMA_SEPARATOR)[1].trim())
                                                .city(geoInfoResponse.getCity())
                                                .build()
                                )
                                .build());
                        Response response = keycloakService.addUser(UserAddRequestDTO.builder()
                                .username(save.getEmail())
                                .email(save.getEmail())
                                .firstName(save.getName().split(" ")[0])
                                .lastName(save.getName().split(" ")[1].trim())
                                .password(PASSWORD)
                                .build());
                        if (response.getStatus() == 201) {
                            keycloakService.assignRoleToUser(response
                                    .getLocation()
                                    .getPath()
                                    .replaceAll(FIND_ID_REGEX, REPLACE_ID_REGEX), UBER_DRIVER);
                            driverList.add(save);
                            userCreatedMessagePublisher.publish(save.getEmail(),
                                    this::generateDriverCreatedMessage, UserType.DRIVER);
                            log.info("Driver added to keycloak , driver : {}", save.getEmail());
                        }
                        else {
                            log.error("Driver not added to keycloak , driver id : {}", save.getId());
                        }
                    });
            return driverList.stream().map(driver -> toDriverListDTO(driver , distance)).toList();
        }
        return Collections.emptyList();
    }

    private void generateDriverCreatedMessage(String s, String s1) {
        log.info("Driver created message published for driver: {}", s);
    }

    private DriverListDTO toDriverListDTO(Driver driver, Double distance) {
        return DriverListDTO.builder()
                .id(driver.getId())
                .name(driver.getName())
                .phone(driver.getPhone())
                .email(driver.getEmail())
                .driverStatus(driver.getDriverStatus())
                .locations(driver.getLocations())
                .price(BigDecimal.valueOf(distance * DRIVER_DEFAULT_PER_KM_PRICE))
                .pendingRequest(Objects.isNull(driver.getPendingRequest()) ?
                        new PendingRequest() : driver.getPendingRequest())
                .ipAddress(driver.getIpAddress())
                .build();
    }

    public List<DriverListDTO> nearDriverList(Double latitude, Double longitude,Double distance) {
        return driverRepository.findAll()
                .stream()
                .filter(driver -> driver.getDriverStatus().equals(DriverStatus.AVAILABLE) &&
                        geoIPLocationService.isNearBy(latitude, longitude, driver.getLocations().getLatitude(), driver.getLocations().getLongitude()))
                .peek(driver -> driver.getLocations().setDistance(geoIPLocationService.getDistance(latitude, longitude,
                                driver.getLocations().getLatitude(),driver.getLocations().getLongitude())
                        / 1000 ) )
                .map(driver -> toDriverListDTO(driver, distance))
                .toList();

    }

    public void setDriverPendingRequest(DriverCallRequestDTO driverCallRequestAvroModelToCallDriverDTO, Driver driver) {
        driver.setPendingRequest(pendingRequestRepository
                .findByRequestId(UUID.fromString(driverCallRequestAvroModelToCallDriverDTO.getRequestId())).orElseThrow(
                        () -> new PendingRequestNotFoundException("No pending request found for request id: " + driverCallRequestAvroModelToCallDriverDTO.getRequestId())));
    }

    public Driver updateStatus(Driver driver) {
        driver.setDriverStatus(DriverStatus.CALL);
        return driver;
    }

    public CallRequestApprovalEvent validateCallRequest(DriverCallRequestDTO callRequestDTO,
                                                        Driver driver,
                                                        List<String> failureMessages) {
        if (!driver.getDriverStatus().equals(DriverStatus.AVAILABLE)) {
            failureMessages.add("Driver is not available");
        }
        if (failureMessages.isEmpty()) {
            pendingRequestRepository.findByRequestId(UUID.fromString(callRequestDTO.getRequestId()))
                    .ifPresent(driver::setPendingRequest);
            return new CallApprovedEvent(driver);
        }
        else return null;
    }

    public void saveDriver(Driver driver) {
        driverRepository.save(driver);
    }

    public List<DriverListDTO> findAllByIpAddress(String ipAddress) {
        return driverRepository.findAllByIpAddress(ipAddress)
                .stream()
                .map(driver -> toDriverListDTO(driver, driver.getLocations().getDistance()))
                .toList();
    }

    public DriverCallResponseAvroModel getDriverCallResponseAvroModel(String sagaId,
                                                                      CallApprovedEventPayload orderEventPayload) {
        return DriverCallResponseAvroModel.newBuilder()
                .setSagaId(sagaId)
                .setId(UUID.randomUUID().toString())
                .setDriverStatus(com.uber.api.kafka.model.DriverStatus.UNAVAILABLE)
                .setFailureMessages(Collections.emptyList())
                .setCreatedAt(Instant.now())
                .setDriverMail(orderEventPayload.getDriverEmail())
                .setPendingRequestId(getPendingRequestId(orderEventPayload))
                .build();
    }

    private String getPendingRequestId(CallApprovedEventPayload orderEventPayload) {
        return driverRepository.findByEmail(orderEventPayload.getDriverEmail()).orElseThrow(
                () -> new DriverNotFoundException("No driver found for email: " + orderEventPayload.getDriverEmail()))
                .getPendingRequest()
                .getRequestId()
                .toString();
    }

    public DriverListDTO getDriverDTO(Driver driver) {
        return DriverListDTO.builder()
                .id(driver.getId())
                .name(driver.getName())
                .email(driver.getEmail())
                .phone(driver.getPhone())
                .driverStatus(driver.getDriverStatus())
                .locations(driver.getLocations())
                .pendingRequest(driver.getPendingRequest())
                .ipAddress(driver.getIpAddress())
                .build();
    }
}
