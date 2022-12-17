package com.uber.api.driver.api.controller;


import com.uber.api.driver.api.dto.DriverCallResponse;
import com.uber.api.driver.api.service.CallDriverRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import javax.validation.constraints.NotBlank;

@Slf4j
@Validated
@RequestScope
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/driver/command", produces = "application/vnd.api.v1+json")
public class CommandDriverController {

    private final CallDriverRequestService driverService;

    @PostMapping("/accept/{requestId}")
    public ResponseEntity<DriverCallResponse> acceptCustomerCallRequest(@PathVariable @NotBlank String requestId) {
        driverService.acceptCustomerCallRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject/{requestId}")
    public ResponseEntity<DriverCallResponse> rejectCustomerCallRequest(@PathVariable @NotBlank String requestId) {
        driverService.rejectCustomerCallRequest(requestId);
        return ResponseEntity.ok().build();
    }
}
