package com.uber.api.driver.api.controller;


import com.uber.api.driver.api.dto.DriverListDTO;
import com.uber.api.driver.api.dto.DriverStatusDTO;
import com.uber.api.driver.api.service.CallDriverRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/driver/query", produces = "application/vnd.api.v1+json")
public class QueryDriverController {

    private final CallDriverRequestService driverService;

    @GetMapping("/list/{ipAddress}")
    public ResponseEntity<List<DriverListDTO>> getDriver(@PathVariable String ipAddress,
                                                  @RequestParam String  distance,
                                                  Principal principal) {
            List<DriverListDTO> driver = driverService.getDriver(ipAddress,Double.valueOf(distance),principal.getName());
            return ResponseEntity.ok(driver);
    }

    @GetMapping("/status/{mail}")
    public ResponseEntity<DriverStatusDTO> getDriverStatus(@PathVariable String mail) {
            var driver = driverService.getDriverStatus(mail);
            return ResponseEntity.ok(driver);
    }

    @GetMapping("/{mail}")
    public DriverListDTO getDriverByMail(@PathVariable String mail) {
        return driverService.getDriverByMail(mail);
    }

}
