package com.uber.api.payment.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CallEventPayload {

    @JsonProperty
    private String paymentId;

    @JsonProperty
    private String customerMail;

    @JsonProperty
    private String requestId;

    @JsonProperty
    private BigDecimal price;

    @JsonProperty
    private String paymentStatus;

    @JsonProperty
    private ZonedDateTime createdAt;

    @JsonProperty
    private List<String> failureMessages;
}
