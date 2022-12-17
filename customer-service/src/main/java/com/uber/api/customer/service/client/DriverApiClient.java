package com.uber.api.customer.service.client;


import com.uber.api.customer.service.dto.DriverDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "driver-service",
    url = "http://localhost:4768/api/driver/query")
public interface DriverApiClient {

    @GetMapping("/{mail}")
    DriverDTO getDriverByMail(@PathVariable String mail);
}
