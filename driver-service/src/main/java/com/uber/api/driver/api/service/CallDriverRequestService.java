package com.uber.api.driver.api.service;

import com.uber.api.driver.api.dto.DriverCallRequestDTO;
import com.uber.api.driver.api.dto.DriverListDTO;
import com.uber.api.driver.api.dto.DriverStatusDTO;

import java.util.List;

public interface CallDriverRequestService {
    void approveCallRequest(DriverCallRequestDTO driverCallRequestAvroModelToCallDriverDTO);
    List<DriverListDTO> getDriver(String ipAddress, Double valueOf, String name);

    void acceptCustomerCallRequest(String requestId);

    DriverStatusDTO getDriverStatus(String mail);

    DriverListDTO getDriverByMail(String mail);

    void rejectCustomerCallRequest(String requestId);

    void processCompleted(DriverCallRequestDTO driverCallRequestAvroModelToCallDriverDTO);


}
