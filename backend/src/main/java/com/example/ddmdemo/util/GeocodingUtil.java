package com.example.ddmdemo.util;

import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class GeocodingUtil {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    private static final WebClient webClient = WebClient.builder()
            .baseUrl(NOMINATIM_URL)
            .defaultHeader("User-Agent", "bogdanovic.r240.2024@uns.ac.rs")
            .defaultHeader("Accept", "application/json")
            .build();

    public static GeoPoint geocode(String location) {

    	Mono<NominatimResponse[]> responseMono = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .queryParam("q", location)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .build())
                .retrieve()
                .bodyToMono(NominatimResponse[].class);

        NominatimResponse[] response = responseMono.block();

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
