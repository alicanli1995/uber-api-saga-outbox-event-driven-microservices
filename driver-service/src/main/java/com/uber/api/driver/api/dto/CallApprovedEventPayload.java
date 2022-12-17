package com.uber.api.driver.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.uber.api.common.api.entity.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CallApprovedEventPayload {
    @JsonProperty
    private String driverEmail;
    @JsonProperty
    private String customerEmail;
    @JsonProperty
    private String requestId;
    @JsonProperty
    private Location customerLocation;
    @JsonProperty
    private Location customerDestination;
    @JsonProperty
    private Double distance;
    @JsonProperty
    private Double price;
}
