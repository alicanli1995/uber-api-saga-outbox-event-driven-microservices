package com.uber.api.geo.location.api.service;

import com.uber.api.common.api.dto.GeoIP;

import java.math.BigDecimal;
import java.util.Map;

public interface GeoIPLocationService {
    GeoIP getIpLocation(String ipAddress) ;

    String getDeviceDetails(String userAgent);

    Map<String,Double[]> randomDriverLocationGenerator(double lat1, double lon1, Integer num);

    BigDecimal getDistance(double lat1, double lon1, double lat2, double lon2, Double height);

    BigDecimal getDistance(double lat1, double lon1, double lat2, double lon2);

    double disc(double lat1, double lon1, double lat2, double lon2);

    boolean isNearBy(Double latitude, Double longitude, double latitude1, double longitude1);


}
