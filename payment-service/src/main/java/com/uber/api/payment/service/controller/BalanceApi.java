package com.uber.api.payment.service.controller;

import com.uber.api.payment.service.service.BalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Slf4j
@Validated
@RequestScope
@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping(value = "/api/balance/query", produces = "application/vnd.api.v1+json")
public class BalanceApi {

    private final BalanceService balanceService;

    @GetMapping("/balance/{mail}")
    public BigDecimal getBalance(@PathVariable @Validated @NotBlank String mail){
        return balanceService.getBalance(mail);
    }
}
