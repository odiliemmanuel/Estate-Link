package com.estatelink.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * Resolves user details (email, name) from user-service so notification
 * emails can reach real recipients even though domain events only carry IDs.
 */
@Slf4j
@Service
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(@Value("${user.service.url}") String userServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(userServiceUrl).build();
    }

    public UserRef findById(UUID userId) {
        if (userId == null) {
            return null;
        }
        try {
            return restClient.get()
                    .uri("/api/v1/users/{id}", userId)
                    .retrieve()
                    .body(UserRef.class);
        } catch (Exception e) {
            log.warn("Could not fetch user {} from user-service: {}", userId, e.getMessage());
            return null;
        }
    }

    public record UserRef(UUID id, String name, String email) {
    }
}
