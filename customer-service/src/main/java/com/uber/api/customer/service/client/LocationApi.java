package com.uber.api.customer.service.client;

import com.uber.api.common.api.dto.GeoIP;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "location-api",
        url = "http://localhost:8090/api/geo")
public interface LocationApi {

    @GetMapping("/location/{ip}")
    GeoIP getIpLocation(@PathVariable String ip);

}
