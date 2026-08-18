package com.estatelink.property.service;

import com.estatelink.property.dto.responses.geocoding.GeocodingResult;
import com.estatelink.property.exception.AddressNotVerifiedException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final RestClient restClient;

    public GeocodingResult geocode(String address, String city, String state) {
        String query = address + ", " + city + ", " + state + ", Nigeria";
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://nominatim.openstreetmap.org/search?format=json&limit=1&q=" + encoded;

        log.info("Geocoding address: {}", query);

        JsonNode response = restClient.get()
                .uri(url)
                .header("User-Agent", "EstateLink/1.0 (kaodilichiejeh02@gmail.com)")
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !response.isArray() || response.isEmpty()) {
            log.warn("Nominatim returned no results for: {}", query);
            throw new AddressNotVerifiedException(
                    "Address could not be verified: '" + query + "'. Please check and try again."
            );
        }

        JsonNode result = response.get(0);
        return new GeocodingResult(
                result.path("lat").asDouble(),
                result.path("lon").asDouble(),           // ← Nominatim uses "lon" not "lng"
                result.path("display_name").asText(),
                result.path("type").asText()
        );
    }
}