package com.uber.api.driver.api.controller;


import com.uber.api.driver.api.client.LocationApi;
import com.uber.api.driver.api.dto.DriverListDTO;
import com.uber.api.driver.api.dto.DriverStatusDTO;
import com.uber.api.driver.api.dto.GeoIP;
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

    private final LocationApi geoIPLocationService;
    private final CallDriverRequestService driverService;

    @GetMapping("/list/{ipAddress}")
    public ResponseEntity<List<DriverListDTO>> getDriver(@PathVariable String ipAddress,
                                                  @RequestParam String  distance,
                                                  Principal principal) {
        try {
            log.info("Querying drivers by username : {}", principal.getName());
            GeoIP ipLocation = geoIPLocationService.getIpLocation(ipAddress);
            List<DriverListDTO> driver = driverService.getDriver(ipAddress,ipLocation.getLatitude(),ipLocation.getLongitude(),Double.valueOf(distance),principal.getName());
            return ResponseEntity.ok(driver);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/status/{mail}")
    public ResponseEntity<DriverStatusDTO> getDriverStatus(@PathVariable String mail) {
        try {
            var driver = driverService.getDriverStatus(mail);
            return ResponseEntity.ok(driver);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{mail}")
    public DriverListDTO getDriverByMail(@PathVariable String mail) {
        return driverService.getDriverByMail(mail);
    }

}
