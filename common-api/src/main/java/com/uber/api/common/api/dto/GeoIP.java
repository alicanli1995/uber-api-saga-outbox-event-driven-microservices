package com.uber.api.common.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoIP {

    private String ipAddress;
    private String device;
    private String city;
    private String fullLocation;
    private Double latitude;
    private Double longitude;

}
