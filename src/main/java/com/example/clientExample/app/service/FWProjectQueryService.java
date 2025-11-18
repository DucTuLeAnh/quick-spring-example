package com.example.clientExample.app.service;

import com.example.clientExample.app.entities.rest.FWProject;
import com.example.clientExample.app.entities.rest.FWProjectResponse;
import com.example.clientExample.shared.FWAccessConfiguration;
import com.example.clientExample.shared.TokenService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FWProjectQueryService {
    private final RestClient restClient;
    private final TokenService tokenService;
    private final FWAccessConfiguration config;

    public FWProjectQueryService(RestClient restClient, TokenService tokenService, FWAccessConfiguration config) {
        this.restClient = restClient;
        this.tokenService = tokenService;
        this.config = config;
    }

    private FWProjectResponse queryWithCursor(Integer cursor){
        FWProjectResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host(config.getFwHost())
                        .port(config.getFwPort())
                        .path("/api/v1/project")
                        .queryParam("cursor", cursor)
                        .build())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                //.header(HttpHeaders.AUTHORIZATION, "Bearer "+ this.fwTokenService.getAuthToken())
                .retrieve()
                .body(FWProjectResponse.class);
        return Optional.ofNullable(response).orElseThrow(() -> new IllegalStateException("Null object received in FW Project Query Service."));
    }

    public List<FWProject> retrieveAllProjects(){

        List<FWProject> allProjects = new ArrayList<>();

        Integer currentCursor = 1;


        while(currentCursor != null && currentCursor != 0) {
            FWProjectResponse response = queryWithCursor(currentCursor);
            allProjects.addAll(response.projects());
            currentCursor = response.nextCursor();
        }


        return allProjects;
    }
}
