package com.example.clientExample.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AppService {

    private final OAuth2AuthorizedClientManager clientManager;
    private final RestClient restClient;

    @Autowired
    public AppService(@Qualifier("authorizedClientManager") OAuth2AuthorizedClientManager clientManager,
                      RestClient.Builder restClientBuilder) {
        this.clientManager = clientManager;
        this.restClient = restClientBuilder.build();
    }

    public String fetchData() {
        // 1. Get an access token
        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
                .withClientRegistrationId("entra")
                .principal("backend-service")
                .build();

        OAuth2AuthorizedClient client = clientManager.authorize(request);
        if (client == null) {
            throw new IllegalStateException("Failed to authorize EntraID client");
        }

        String token = client.getAccessToken().getTokenValue();

        // 2. Use the token to call the remote API
        return restClient.get()
                .uri("https://api.example.com/data")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(String.class);
    }
}
