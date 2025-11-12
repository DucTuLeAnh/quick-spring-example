package com.example.clientExample.app.service;

import com.example.clientExample.app.entities.Event;
import com.example.clientExample.app.entities.EventResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Service
public class EventQueryService {
    private RestClient restClient;
    private final FwTokenService fwTokenService;

    public EventQueryService(RestClient restClient, FwTokenService fwTokenService) {
        this.restClient = restClient;
        this.fwTokenService = fwTokenService;
    }


    public List<Event> retrieveAllEventsByDate(String from, String to){
        EventResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("testurl")
                        .port(25000)
                        .path("/api/v1/event")
                        .queryParam("dayFrom", from)
                        .queryParam("dayTo", to)
                        .build())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+ this.fwTokenService.getAuthToken())
                .retrieve()
                .body(EventResponse.class);

        return Optional.ofNullable(response).map(EventResponse::events).orElse(List.of());
    }
}
