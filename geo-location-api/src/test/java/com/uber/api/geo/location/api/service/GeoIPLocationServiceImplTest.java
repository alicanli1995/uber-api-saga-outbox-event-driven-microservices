package com.uber.api.geo.location.api.service;


import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;

import com.uber.api.common.api.dto.GeoIP;
import com.uber.api.geo.location.api.service.impl.GeoIPLocationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@Import(GeoIPLocationServiceImpl.class)
class GeoIPLocationServiceImplTest {

    @Autowired
    private GeoIPLocationService geoIPLocationService;

    @MockBean
    private DatabaseReader databaseReader;


    @Test
    void test_getIpLocation_whenIpIsValid() throws IOException, GeoIp2Exception {
        CityResponse cityResponse = new CityResponse(
                new City(
                        List.of("city", "city2", "city3"),
                        0,
                        0L,
                        Map.of("en", "city")
                ),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(databaseReader.city(any())).thenReturn(cityResponse);

        GeoIP geoIPLocationResponse = geoIPLocationService.getIpLocation(anyString());

        assertThat(geoIPLocationResponse).isNotNull();
    }

    @Test
    void test_getIpLocation_whenIpIsInvalid() throws IOException, GeoIp2Exception {
        when(databaseReader.city(any())).thenThrow(new GeoIp2Exception("Invalid IP"));

        GeoIP geoIPLocationResponse = geoIPLocationService.getIpLocation(anyString());

        assertThat(geoIPLocationResponse.getFullLocation()).isNull();
        assertThat(geoIPLocationResponse.getCity()).isNull();
        assertThat(geoIPLocationResponse.getLatitude()).isNull();
        assertThat(geoIPLocationResponse.getLongitude()).isNull();

    }

    @Test
    void test_getDeviceDetails_whenUserAgentIsValid(){
        String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36";

        String deviceDetails = geoIPLocationService.getDeviceDetails(userAgent);

        assertThat(deviceDetails).isNotNull();
        assertThat(deviceDetails).isNotEmpty();
    }

    @Test
    void test_randomDriverLocationGenerator_whenParamsAreValid(){

        var driverLocationGenerator = geoIPLocationService.randomDriverLocationGenerator(23.0, 23.0, 2);

        assertThat(driverLocationGenerator).isNotNull();
        assertThat(driverLocationGenerator.get("Location1")).isNotNull();
        assertThat(Arrays.stream(driverLocationGenerator.get("Location1")).count()).isEqualTo(3);
        assertThat(driverLocationGenerator.get("Location0")).isNotNull();
        assertThat(Arrays.stream(driverLocationGenerator.get("Location0")).count()).isEqualTo(3);

    }

    @Test
    void test_isNearby_whenParamsAreValid(){

        var driverLocationGenerator = geoIPLocationService.isNearBy(23.0, 23.0, 23.0, 23.0);

        assertThat(driverLocationGenerator).isTrue();
    }

    @Test
    void test_getDistance_withDefaultRange_whenParamsAreValid(){

        var driverLocationGenerator = geoIPLocationService.getDistance(23.0, 23.0, 23.0, 23.0);

        assertThat(driverLocationGenerator).isNotNull();
        assertThat(driverLocationGenerator).isEqualTo(new BigDecimal("400.00"));
    }
}
