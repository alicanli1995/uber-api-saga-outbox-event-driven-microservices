package com.uber.api.geo.location.api.service.impl;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.uber.api.common.api.dto.GeoIP;
import com.uber.api.geo.location.api.service.GeoIPLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua_parser.Client;
import ua_parser.Parser;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static com.uber.api.common.api.constants.CONSTANTS.R;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoIPLocationServiceImpl implements GeoIPLocationService {

    private final DatabaseReader databaseReader;
    private static final String UNKNOWN = "UNKNOWN";

    @Override
    public GeoIP getIpLocation(String ip)  {

        GeoIP position = new GeoIP();
        String location;
        String continent;
        String country;
        CityResponse cityResponse;
        InetAddress ipAddress;

        try {
            ipAddress = InetAddress.getByName(ip);
            cityResponse = databaseReader.city(ipAddress);
        }catch (IOException | GeoIp2Exception e) {
            log.error("Error while getting location for ip: {}", ip, e);
            return position;
        }


        if (nonNull(cityResponse) && nonNull(cityResponse.getCity())) {

            continent = (cityResponse.getContinent() != null) ? cityResponse.getContinent().getName() : "";
            country = (cityResponse.getCountry() != null) ? cityResponse.getCountry().getName() : "";

            location = String.format("%s, %s, %s", continent, country, cityResponse.getCity().getName());
            position.setCity(cityResponse.getCity().getName());
            position.setFullLocation(location);
            position.setLatitude(Objects.nonNull(cityResponse.getLocation()) && (Objects.nonNull(cityResponse.getLocation().getLatitude())) ? cityResponse.getLocation().getLatitude() : 0);
            position.setLongitude(Objects.nonNull(cityResponse.getLocation()) && (Objects.nonNull(cityResponse.getLocation().getLongitude())) ? cityResponse.getLocation().getLongitude() : 0);
            position.setIpAddress(ip);
        }

        return position;
    }

    @Override
    public String getDeviceDetails(String userAgent) {
        StringBuilder deviceDetails = new StringBuilder();

        deviceDetails.append(UNKNOWN);

        Parser parser = new Parser();

        Client client = parser.parse(userAgent);

        if (nonNull(client)) {
            deviceDetails.delete(0, deviceDetails.length());
            deviceDetails.append(client.userAgent.family).append(" ")
                    .append(client.userAgent.major).append(".")
                    .append(client.userAgent.minor).append(" - ")
                    .append(client.os.family).append(" ")
                    .append(client.os.major).append(".")
                    .append(client.os.minor);
        }

        return deviceDetails.toString();
    }

    @Override
    public Map<String,Double[]> randomDriverLocationGenerator(double lat1, double lon1, Integer num) {
        num = (Objects.isNull(num)) ? 10 : num;
        List<Double> randomValueList = ThreadLocalRandom
                .current()
                .doubles(0.01, 0.09)
                .limit(num)
                .boxed().toList();

        List<Double> randomForHeightList = ThreadLocalRandom
                .current()
                .doubles(100, 500)
                .limit(num)
                .boxed().toList();

        List<Double> latList = randomValueList.stream()
                .map(randomValue -> lat1 + randomValue).toList();

        List<Double> lonList = randomValueList.stream()
                .map(randomValue -> lon1 + randomValue).toList();

        Map<String, Double[]> locationMap = new HashMap<>();

        Stream.iterate(0, i -> i + 1)
                .limit(num)
                .forEach(i -> {
                    BigDecimal bd  = getDistance(lat1, lon1, latList.get(i), lonList.get(i), randomForHeightList.get(i));
                    locationMap.put("Location" + i, new Double[]{latList.get(i), lonList.get(i),
                            bd.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP).doubleValue()});
                });

        return locationMap;
    }

    @Override
    public BigDecimal getDistance(double lat1, double lon1, double lat2, double lon2, Double height) {
        return BigDecimal.valueOf(Math.sqrt(Math.pow(disc(lat1, lon1, lat2, lon2), 2) + Math.pow(height, 2)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getDistance(double lat1, double lon1, double lat2, double lon2) {
        return BigDecimal.valueOf(Math.sqrt(Math.pow(disc(lat1, lon1, lat2, lon2), 2) + Math.pow(400, 2)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public double disc(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // convert to meters
    }
    @Override
    public boolean isNearBy(Double latitude, Double longitude, double latitude1, double longitude1) {
        return disc(latitude, longitude, latitude1, longitude1) < 10000;
    }

}
