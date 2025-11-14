package com.example.clientExample.app.service;

import com.example.clientExample.app.entities.rest.TokenActiveResponse;
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

    //todo: extract to own files
    public record AuthCredentials(String appID, String userName, String key) {
    }

    public record TokenResponse(Integer isError, String accessToken) {
    }

    public String getAuthToken() {
        /*
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
*/
        return "mydebugtokenwillbesethere";

    }


    public boolean isTokenValid(String token) {
        TokenActiveResponse response = restClient.get()
                .uri(fwAccessConfiguration.getBaseApiUrl() + AUTH_PATH)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(TokenActiveResponse.class);

        return Optional.ofNullable(response).map(r -> !r.isError()).orElse(false);
    }


}
