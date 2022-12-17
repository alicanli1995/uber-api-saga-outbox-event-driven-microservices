package com.uber.api.customer.service.controller;

import com.uber.api.customer.service.dto.CustomerStatusDTO;
import com.uber.api.customer.service.service.CustomerCallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/customer/query", produces = "application/vnd.api.v1+json")
public class CustomerQueryController {

    private final CustomerCallService customerCallService;

    @GetMapping("/{mail}")
    public ResponseEntity<CustomerStatusDTO> getCustomerStatus(@PathVariable String mail,
                                                               @RequestParam String ip,
                                                               Principal principal) {
        try {
            var customerStatusDTO = customerCallService.getCustomerStatus(mail,principal.getName(),ip);
            return ResponseEntity.ok(customerStatusDTO);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
}
