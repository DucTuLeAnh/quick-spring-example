package com.example.clientExample.app.service;

import com.example.clientExample.app.entities.FWObject;
import com.example.clientExample.app.entities.FWObjectResponse;
import com.example.clientExample.app.entities.FWProject;
import com.example.clientExample.app.entities.FWProjectResponse;
import com.example.clientExample.shared.FWAccessConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class FWProjectQueryService {
    private final RestClient restClient;
    private final FWTokenService fwTokenService;
    private final FWAccessConfiguration config;

    public FWProjectQueryService(RestClient restClient, FWTokenService fwTokenService, FWAccessConfiguration config) {
        this.restClient = restClient;
        this.fwTokenService = fwTokenService;
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
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+ this.fwTokenService.getAuthToken())
                .retrieve()
                .body(FWProjectResponse.class);
        return Optional.ofNullable(response).orElseThrow(() -> new IllegalStateException("Null object received in FW Project Query Service."));
    }

    public List<FWProject> retrieveAllRoomObjects(){

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
