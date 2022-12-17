package com.uber.api.driver.api.client;

import com.uber.api.driver.api.dto.GeoIP;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(value = "location-api",
        url = "http://localhost:8090/api/geo")
public interface LocationApi {

    @GetMapping("/random/location/{num}")
    Map<String,Double[]> randomDriverLocationGenerator(@PathVariable(required = false) Integer num,
                                                       @RequestParam double lat1,
                                                       @RequestParam double lon1);

    @GetMapping("/location/{ip}")
    GeoIP getIpLocation(@PathVariable String ip);

    @GetMapping("/distance/height")
    Double getDistance(@RequestParam double lat1,
                       @RequestParam double lon1,
                       @RequestParam double lat2,
                       @RequestParam double lon2,
                       @RequestParam Double height);

    @GetMapping("/distance")
    Double getDistance(@RequestParam double lat1,
                       @RequestParam double lon1,
                       @RequestParam double lat2,
                       @RequestParam double lon2);

    @GetMapping("/disc")
    Double disc(@RequestParam double lat1,
                @RequestParam double lon1,
                @RequestParam double lat2,
                @RequestParam double lon2);

    @GetMapping("/nearby")
    boolean isNearBy(@RequestParam Double latitude,
                            @RequestParam Double longitude,
                            @RequestParam double latitude1,
                            @RequestParam double longitude1);
}
