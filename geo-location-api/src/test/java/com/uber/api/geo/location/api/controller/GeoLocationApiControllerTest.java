package com.uber.api.geo.location.api.controller;

import com.uber.api.common.api.dto.GeoIP;
import com.uber.api.geo.location.api.service.GeoIPLocationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GeoLocationApi.class)
class GeoLocationApiControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GeoIPLocationService geoIPLocationService;

    @Test
    void test_randomDriverLocationGenerator_with_valid_input() throws Exception {
        Map<String,Double[]> randomDriverLocationGenerator = Map.of(
                "Location0", new Double[]{1.0, 1.0},
                "Location1", new Double[]{2.0, 2.0}
        );

        given(geoIPLocationService.randomDriverLocationGenerator(anyDouble(),anyDouble(),anyInt())).willReturn(randomDriverLocationGenerator);

        var resultActions = mockMvc.perform(get("/api/geo/random/location/2?lat1=1.0&lon1=1.0"))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.api.v1+json"))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$", hasKey("Location0")))
                .andExpect(jsonPath("$", hasKey("Location1")));

    }

    @Test
    void test_getIpLocation_with_valid_input() throws Exception {
        GeoIP geoIP = GeoIP.builder()
                .ipAddress("192.168.1.1")
                .fullLocation("United States")
                .longitude(1.0)
                .latitude(1.0)
                .city("New York")
                .device("Computer")
                .build();

        given(geoIPLocationService.getIpLocation(anyString())).willReturn(geoIP);

        var resultActions = mockMvc.perform(get("/api/geo/location/192.168.1.1" ))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.api.v1+json"))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.ipAddress", notNullValue()))
                .andExpect(jsonPath("$.fullLocation", notNullValue()))
                .andExpect(jsonPath("$.city", notNullValue()))
                .andExpect(jsonPath("$.latitude", notNullValue()))
                .andExpect(jsonPath("$.longitude", notNullValue()));

    }

    @Test
    void void_getDistanceBetweenTwoLocations_with_valid_input() throws Exception {
        given(geoIPLocationService.getDistance(anyDouble(),anyDouble(),anyDouble(),anyDouble())).willReturn(new BigDecimal("1.0"));

        var resultActions = mockMvc.perform(get("/api/geo/distance?lat1=1.0&lon1=1.0&lat2=2.0&lon2=2.0" ))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.api.v1+json"))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$", is(1.0)));
    }

    @Test
    void void_getDistanceBetweenTwoLocations_with_valid_input_and_height() throws Exception {
        given(geoIPLocationService.getDistance(anyDouble(),anyDouble(),anyDouble(),anyDouble(),anyDouble()))
                .willReturn(new BigDecimal("1.0"));

        var resultActions = mockMvc.perform(get("/api/geo/distance/height?lat1=1.0&lon1=1.0&lat2=2.0&lon2=2.0&height=100" ))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.api.v1+json"))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$", is(1.0)));
    }

    @Test
    void test_disc_distance_with_valid_input() throws Exception {
        given(geoIPLocationService.disc(anyDouble(),anyDouble(),anyDouble(),anyDouble())).willReturn(1.0);

        var resultActions = mockMvc.perform(get("/api/geo/disc?lat1=1.0&lon1=1.0&lat2=2.0&lon2=2.0"))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.api.v1+json"))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$", is(1.0)));
    }

    @Test
    void test_isNearBy_with_valid_input() throws Exception {
        given(geoIPLocationService.isNearBy(anyDouble(),anyDouble(),anyDouble(),anyDouble())).willReturn(true);

        var resultActions = mockMvc.perform(get("/api/geo/nearby?latitude=1.0&longitude=1.0&latitude1=2.0&longitude1=2.0"))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.api.v1+json"))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$", is(true)));
    }

}
