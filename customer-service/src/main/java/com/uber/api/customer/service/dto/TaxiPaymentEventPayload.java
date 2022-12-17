package com.uber.api.customer.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxiPaymentEventPayload {
    @JsonProperty
    private String requestId;
    @JsonProperty
    private String customerMail;
    @JsonProperty
    private BigDecimal price;
    @JsonProperty
    private ZonedDateTime createdAt;
    @JsonProperty
    private String paymentStatus;
}
