package com.example.clientExample.app.service;

import com.example.clientExample.app.entities.TokenActiveResponse;
import com.example.clientExample.shared.FWAccessConfiguration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Optional;

@Service
@Scope("application")
public class FWTokenService {


    private final RestClient restClient;
    private final FWAccessConfiguration fwAccessConfiguration;
    private final String AUTH_PATH = "auth/token";

    public FWTokenService(RestClient restClient, FWAccessConfiguration fwAccessConfiguration) {
        this.restClient = restClient;
        this.fwAccessConfiguration = fwAccessConfiguration;
    }

    public String testQuery() {

        String dayFrom = "2025-11-01";
        String dayTo = "2025-11-11";
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host(fwAccessConfiguration.getFwHost())
                        .port(fwAccessConfiguration.getFwPort())
                        .path("/api/v1/event")
                        .queryParam("dayFrom", dayFrom)
                        .queryParam("dayTo", dayTo)
                        .build())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.getAuthToken())
                .retrieve()
                .body(String.class);

    }


    public record AuthCredentials(String appID, String userName, String key) {
    }

    public record TokenResponse(Integer isError, String accessToken) {
    }

    public String getAuthToken() {


        TokenResponse response = restClient.post()
                .uri(fwAccessConfiguration.getBaseApiUrl() + AUTH_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(new AuthCredentials(fwAccessConfiguration.getFwAppId(), fwAccessConfiguration.getFwUserName(), fwAccessConfiguration.getFwKey()))
                .retrieve()
                .body(TokenResponse.class);


        System.out.println("This is the response: " + response);
        //todo: dont return empty string rather throw exception
        return Optional.ofNullable(response).map(TokenResponse::accessToken).orElse("");
    }


    public boolean isTokenValid(String token) {
        //restClient.get()
        TokenActiveResponse response = restClient.get()
                .uri(fwAccessConfiguration.getBaseApiUrl() + AUTH_PATH)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(TokenActiveResponse.class);

        return Optional.ofNullable(response).map(r -> !r.isError()).orElse(false);
    }


}
