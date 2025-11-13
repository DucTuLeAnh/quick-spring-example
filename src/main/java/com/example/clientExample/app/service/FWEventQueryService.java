package com.example.clientExample.app.service;

import com.example.clientExample.app.entities.FWEvent;
import com.example.clientExample.app.entities.FWEventResponse;
import com.example.clientExample.shared.FWAccessConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FWEventQueryService {

    public static final String PROJECT_EVENT_TYPE = "project";
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


    private FWEventResponse retrieveAllProjectEventsByCursor(String authToken, LocalDate from, LocalDate to, Integer cursor){
        FWEventResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host(config.getFwHost())
                        .port(config.getFwPort())
                        .path("/api/v1/event")
                        .queryParam("dayFrom", from.toString())
                        .queryParam("dayTo", to.toString())
                        .queryParam("eventType", PROJECT_EVENT_TYPE)
                        .queryParam("cursor", cursor)
                        .build()
                )
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .retrieve()
                .body(FWEventResponse.class);

        return Optional.ofNullable(response).orElseThrow(() -> new IllegalStateException("Null object received in FW Project Query Service."));
    }

    public List<FWEvent> retrieveAllProjectEvents(LocalDate from, LocalDate to, List<String> objectIds, List<String> projectIds) {

        if (from == null || to == null) {
            return Collections.emptyList();
        }

        String authToken = this.fwTokenService.getAuthToken();

        // use hashmap for instant lookup and avoiding quadratic search.
        Set<String> objectIdSet = new HashSet<>(objectIds);
        Set<String> projectIdSet = new HashSet<>(projectIds);

        //UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()

        List<FWEvent> allEvents = new ArrayList<>();

        Integer currentCursor = 1;

        while(currentCursor != null && currentCursor != 0) {
            FWEventResponse response = retrieveAllProjectEventsByCursor(authToken, from, to.plusDays(1), currentCursor);
            allEvents.addAll(response.events());
            currentCursor = response.nextCursor();
        }


        if (!objectIds.isEmpty()) {
            allEvents = allEvents.stream()
                    .filter(event -> objectIdSet.contains(event.objectID()))
                    .toList();
        }

        if(!projectIds.isEmpty()){
            allEvents = allEvents.stream()
                    .filter(event -> projectIdSet.contains(event.projectID()))
                    .toList();
        }


        LocalDateTime rangeStart = from.atTime(3, 0);                 // startDate 03:00
        LocalDateTime rangeEnd   = from.plusDays(1).atTime(2, 59, 59, 999_999_999);

        return allEvents.stream()
                .filter(e -> {
                    LocalDateTime ts = e.dateTimeInAsString();
                    return !ts.isBefore(rangeStart) && !ts.isAfter(rangeEnd);
                })
                .sorted(Comparator.comparing(FWEvent::dateTimeInAsString))
                .toList();

    }
}
