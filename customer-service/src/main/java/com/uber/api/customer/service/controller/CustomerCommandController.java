package com.uber.api.customer.service.controller;


import com.uber.api.customer.service.dto.CallDriverCommand;
import com.uber.api.customer.service.dto.CallStatusDTO;
import com.uber.api.customer.service.service.CustomerCallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/customer/command", produces = "application/vnd.api.v1+json")
public class CustomerCommandController {
    private final CustomerCallService customerCallService;

    @PostMapping("/call")
    public ResponseEntity<CallStatusDTO> createTaxiCall(@RequestBody @Valid CallDriverCommand callDriverCommand) {
        log.info("Call Driver with command: {}", callDriverCommand);
        var callDriver = customerCallService.callDriver(callDriverCommand);
        log.info("Created driver request with tracking id: {}", callDriverCommand.pendingRequest().getRequestId());
        return ResponseEntity.ok(callDriver);
    }


}
