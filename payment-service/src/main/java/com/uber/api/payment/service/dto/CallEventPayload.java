package com.uber.api.payment.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallEventPayload {

    @JsonProperty("paymentId")
    private String paymentId;

    @JsonProperty("customerMail")
    private String customerMail;

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("paymentStatus")
    private String paymentStatus;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("failureMessages")
    private List<String> failureMessages;
}
