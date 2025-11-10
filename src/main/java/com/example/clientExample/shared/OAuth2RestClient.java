package com.example.clientExample.shared;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OAuth2RestClient {

    private final OAuth2AuthorizedClientManager clientManager;
    private final RestClient restClient;

    @Autowired
    public OAuth2RestClient(@Qualifier("authorizedClientManager") OAuth2AuthorizedClientManager clientManager,
                      RestClient.Builder restClientBuilder) {
        this.clientManager = clientManager;
        this.restClient = restClientBuilder.build();
    }
    private <T> T get(String url, String token, Class<T> responseType) {
        return restClient.get()
                .uri(url)
                //.header("Authorization", "Bearer " + token)
                .retrieve()
                .body(responseType);
    }
    public <T> T fetchData(String url,Class<T> responseType) {
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
        return this.get(url, "token", responseType);
    }
}