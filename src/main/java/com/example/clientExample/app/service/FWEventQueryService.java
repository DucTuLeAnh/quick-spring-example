package com.example.clientExample.app.service;

import com.example.clientExample.app.entities.FWEvent;
import com.example.clientExample.app.entities.FWEventResponse;
import com.example.clientExample.shared.FWAccessConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.*;

@Service
public class FWEventQueryService {
    private final RestClient restClient;
    private final FWTokenService fwTokenService;
    private final FWAccessConfiguration config;

    public FWEventQueryService(RestClient restClient, FWTokenService fwTokenService, FWAccessConfiguration config) {
        this.restClient = restClient;
        this.fwTokenService = fwTokenService;
        this.config = config;
    }

// todo: retrieve all info via cursor
    public List<FWEvent> retrieveAllEventsByDate(LocalDate from, LocalDate to, List<String> objectIds, List<String> projectIds) {

        if (from == null || to == null) {
            return Collections.emptyList();
        }

        String authToken = this.fwTokenService.getAuthToken();

        // use hashmap for instant lookup and avoiding quadratic search.
        Set<String> objectIdSet = new HashSet<>(objectIds);
        Set<String> projectIdSet = new HashSet<>(projectIds);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(config.getFwHost())
                .port(config.getFwPort())
                .path("/api/v1/event")
                .queryParam("dayFrom", from.toString())
                .queryParam("dayTo", to.toString());

        FWEventResponse response = restClient.get()
                .uri(uriBuilder.build().toUri())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .retrieve()
                .body(FWEventResponse.class);

        List<FWEvent> events = Optional.ofNullable(response).map(FWEventResponse::events).orElse(List.of());

        if (!objectIds.isEmpty()) {
            events = events.stream()
                    .filter(event -> objectIdSet.contains(event.objectID()))
                    .toList();

        }

        if(!projectIds.isEmpty()){
            events = events.stream()
                    .filter(event -> projectIdSet.contains(event.projectID()))
                    .toList();
        }


        return events;
    }
}
