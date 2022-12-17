package com.uber.api.driver.api.service.impl;

import com.uber.api.common.api.constants.CallStatus;
import com.uber.api.common.api.constants.DriverStatus;
import com.uber.api.common.api.entity.PendingRequest;
import com.uber.api.driver.api.client.LocationApi;
import com.uber.api.driver.api.dto.*;
import com.uber.api.driver.api.entity.Driver;
import com.uber.api.driver.api.event.CallRequestApprovalEvent;
import com.uber.api.driver.api.exception.DriverHasNotCallException;
import com.uber.api.driver.api.exception.DriverNotFoundException;
import com.uber.api.driver.api.helper.DriverApiHelper;
import com.uber.api.driver.api.outbox.helper.DriverRequestOutboxHelper;
import com.uber.api.driver.api.repository.DriverRepository;
import com.uber.api.driver.api.service.CallDriverRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.uber.api.common.api.constants.CONSTANTS.DRIVER_DEFAULT_PER_KM_PRICE;


@Slf4j
@Service
@RequiredArgsConstructor
public class CallDriverRequestServiceImpl implements CallDriverRequestService {
    private final DriverRepository driverRepository;
    private final DriverRequestOutboxHelper driverOutboxHelper;
    private final DriverApiHelper driverApiHelper;
    private final LocationApi locationApi;

    @Override
    public void approveCallRequest(DriverCallRequestDTO driverCallRequestAvroModelToCallDriverDTO) {
        if (driverApiHelper.publishIfOutboxMessageProcessed(driverCallRequestAvroModelToCallDriverDTO)) {
            log.info("Driver call request approved for request id: {}", driverCallRequestAvroModelToCallDriverDTO.getRequestId());
            return;
        }

        log.info("Driver call request not approved for request id: {}", driverCallRequestAvroModelToCallDriverDTO.getRequestId());
        List<String> failureMessages = new ArrayList<>();
        Driver driver = driverApiHelper.findDriver(driverCallRequestAvroModelToCallDriverDTO);
        CallRequestApprovalEvent callRequestApprovalEvent = driverApiHelper.validateCallRequest(
                driverCallRequestAvroModelToCallDriverDTO,
                driver,
                failureMessages);

        driverOutboxHelper.saveRequestOutboxMessage(driverCallRequestAvroModelToCallDriverDTO,
                callRequestApprovalEvent);
        driverApiHelper.setDriverPendingRequest(driverCallRequestAvroModelToCallDriverDTO, driver);
        driverApiHelper.saveDriver(driverApiHelper.updateStatus(driver));
    }

    @Override
    public List<DriverListDTO> getDriver(String ipAddress, Double distance, String name) {
        GeoIP ipLocation = locationApi.getIpLocation(ipAddress);

        var driverByIpAddress = new ArrayList<>(driverApiHelper.findAllByIpAddress(ipAddress)
                .stream()
                .filter(driver -> driver.getDriverStatus().equals(DriverStatus.AVAILABLE))
                .peek(driver -> driver.setPrice(BigDecimal.valueOf(distance * DRIVER_DEFAULT_PER_KM_PRICE)))
                .toList());
        if(driverByIpAddress.isEmpty()){
            var nearList =  driverApiHelper.nearDriverList(ipLocation.getLatitude(), ipLocation.getLongitude(),distance).stream()
                    .filter(driver -> driver.getDriverStatus().equals(DriverStatus.AVAILABLE))
                    .toList();
            if (nearList.isEmpty()) {
                return driverApiHelper.generateDummyDriverList(GeoIP.builder()
                        .ipAddress(ipAddress)
                        .latitude(ipLocation.getLatitude())
                        .longitude(ipLocation.getLongitude())
                        .build(),
                        distance);
            }
            return nearList;
        }
        driverByIpAddress.addAll(driverApiHelper.nearDriverList(ipLocation.getLatitude(), ipLocation.getLongitude(),distance)
                .stream()
                .filter(driver -> driver.getDriverStatus().equals(DriverStatus.AVAILABLE) && !driverByIpAddress.contains(driver))
                .toList());
        return driverByIpAddress.stream().distinct().toList();
    }


    @Override
    public void acceptCustomerCallRequest(String requestId) {
        var driver = driverRepository.findByPendingRequest(PendingRequest.builder().requestId(UUID.fromString(requestId)).build())
                .orElseThrow(() -> new DriverNotFoundException("Driver not found for request id: " + requestId));
        if (driver.getDriverStatus().equals(DriverStatus.CALL)) {
            driver.getPendingRequest().setCallStatus(CallStatus.ACCEPTED);
            driver.setDriverStatus(DriverStatus.UNAVAILABLE);
            driverRepository.save(driver);
        }
        else {
            throw new DriverHasNotCallException("Driver not in call status for request id: " + requestId);
        }
    }

    @Override
    public DriverStatusDTO getDriverStatus(String mail) {
        Driver driver = getDriver(mail);
        if (driver.getDriverStatus().equals(DriverStatus.CALL) ||
                driver.getDriverStatus().equals(DriverStatus.UNAVAILABLE)) {
            return DriverStatusDTO.builder()
                    .status(driver.getDriverStatus().toString())
                    .requestDTO(DriverRequestDTO.builder()
                            .requestId(driver.getPendingRequest().getRequestId().toString())
                            .customerDestination(driver.getPendingRequest().getCustomerDestination())
                            .customerEmail(driver.getPendingRequest().getCustomerEmail())
                            .customerLocation(driver.getPendingRequest().getCustomerLocation())
                            .customerName(driver.getPendingRequest().getCustomerEmail())
                            .distance(driver.getPendingRequest().getCustomerLocation().getDistance())
                            .price(driver.getPendingRequest().getOffer())
                            .isSpecialOffer(driver.getPendingRequest().getIsSpecialOffer())
                            .build())
                    .build();
        }
        return DriverStatusDTO.builder()
                .status(driver.getDriverStatus().toString())
                .build();
    }

    private Driver getDriver(String mail) {
        return driverRepository.findByEmail(mail)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found for mail: " + mail));
    }

    @Override
    public DriverListDTO getDriverByMail(String mail) {
        return driverApiHelper.getDriverDTO(driverRepository.findByEmail(mail)
                .orElseThrow(() -> new RuntimeException("Driver not found for mail: " + mail)));
    }

    @Override
    public void rejectCustomerCallRequest(String requestId) {
        var driver = driverRepository.findByPendingRequest(PendingRequest.builder().requestId(UUID.fromString(requestId)).build())
                .orElseThrow(() -> new DriverNotFoundException("Driver not found for request id: " + requestId));
        if (driver.getDriverStatus().equals(DriverStatus.CALL)) {
            driver.getPendingRequest().setCallStatus(CallStatus.DRIVER_REJECTED);
            driver.setDriverStatus(DriverStatus.AVAILABLE);
            driverRepository.save(driver);
        }
        else {
            throw new DriverHasNotCallException("Driver not in call status for request id: " + requestId);
        }
    }

    @Override
    public void processCompleted(DriverCallRequestDTO driverCallRequestAvroModelToCallDriverDTO) {
        var driver = getDriver(driverCallRequestAvroModelToCallDriverDTO.getDriverEmail());
        driver.setDriverStatus(DriverStatus.AVAILABLE);
        driver.setPendingRequest(null);
        driverRepository.save(driver);
    }


}
