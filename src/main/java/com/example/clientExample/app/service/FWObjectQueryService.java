package com.example.clientExample.app.service;


import com.example.clientExample.app.entities.rest.FWObject;
import com.example.clientExample.app.entities.rest.FWObjectResponse;
import com.example.clientExample.shared.FWAccessConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Service
public class FWObjectQueryService {
    private final RestClient restClient;
    private final FWTokenService fwTokenService;

    private final FWAccessConfiguration config;

    public FWObjectQueryService(RestClient restClient, FWTokenService fwTokenService, FWAccessConfiguration config) {
        this.restClient = restClient;
        this.fwTokenService = fwTokenService;
        this.config = config;
    }

    public List<FWObject> retrieveAllRoomObjects(){
        FWObjectResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host(config.getFwHost())
                        .port(config.getFwPort())
                        .path("/api/v1/object")
                        .queryParam("getOthers", 1)
                        .build())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+ this.fwTokenService.getAuthToken())
                .retrieve()
                .body(FWObjectResponse.class);

        return Optional.ofNullable(response)
                .map(FWObjectResponse::objects)
                .orElse(Collections.emptyList())
                .stream()
                .filter(fwObject -> fwObject.type().equals("room"))
                .toList();
    }
}
