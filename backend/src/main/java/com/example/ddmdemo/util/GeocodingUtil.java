package com.example.ddmdemo.util;

import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class GeocodingUtil {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    public static GeoPoint geocode(String location) {
    	location = location.replace(" ", "+");
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(NOMINATIM_URL)
            .queryParam("q", location)
            .queryParam("format", "json")
            .queryParam("limit", 1);
        

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "bogdanovic.r240.2024@uns.ac.rs");
        
        String uri = builder.toUriString();

        HttpEntity<String> entity = new HttpEntity<>(headers);

        var responseEntity = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            NominatimResponse[].class
        );

        var response = responseEntity.getBody();

        if (response == null || response.length == 0) {
            throw new RuntimeException("No geocoding result for location: " + location);
        }

        NominatimResponse result = response[0];
        return new GeoPoint(Double.parseDouble(result.lat), Double.parseDouble(result.lon));
    }

    private static class NominatimResponse {
        public String lat;
        public String lon;
    }
}
