package com.uber.api.geo.location.api.controller;

import com.uber.api.common.api.dto.GeoIP;
import com.uber.api.geo.location.api.service.GeoIPLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Validated
@RequestScope
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/geo", produces = "application/vnd.api.v1+json")
public class GeoLocationApi {

    private final GeoIPLocationService geoIPLocationService;

    @GetMapping("/random/location/{num}")
    public Map<String,Double[]> randomDriverLocationGenerator(@PathVariable(required = false) Integer num,
                                                              @RequestParam double lat1,
                                                              @RequestParam double lon1) {
        return geoIPLocationService.randomDriverLocationGenerator(lat1,lon1,num);
    }

    @GetMapping("/location/{ip}")
    public GeoIP getIpLocation(@PathVariable String ip) {
        return geoIPLocationService.getIpLocation(ip);
    }

    @GetMapping("/distance/height")
    public BigDecimal getDistance(@RequestParam double lat1,
                                  @RequestParam double lon1,
                                  @RequestParam double lat2,
                                  @RequestParam double lon2,
                                  @RequestParam double height) {
        return geoIPLocationService.getDistance(lat1,lon1,lat2,lon2,height);
    }

    @GetMapping("/distance")
    public BigDecimal getDistance(@RequestParam double lat1,
                                  @RequestParam double lon1,
                                  @RequestParam double lat2,
                                  @RequestParam double lon2) {
        return geoIPLocationService.getDistance(lat1,lon1,lat2,lon2);
    }

    @GetMapping("/disc")
    public double disc(@RequestParam double lat1,
                       @RequestParam double lon1,
                       @RequestParam double lat2,
                       @RequestParam double lon2) {
        return geoIPLocationService.disc(lat1,lon1,lat2,lon2);
    }

    @GetMapping("/nearby")
    public boolean isNearBy(@RequestParam double latitude,
                            @RequestParam double longitude,
                            @RequestParam double latitude1,
                            @RequestParam double longitude1) {
        return geoIPLocationService.isNearBy(latitude,longitude,latitude1,longitude1);
    }



}
