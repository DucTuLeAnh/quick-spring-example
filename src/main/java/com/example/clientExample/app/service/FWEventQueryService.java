package com.example.clientExample.app.service;

import com.example.clientExample.app.entities.FWCustoms;
import com.example.clientExample.app.entities.FWEvent;
import com.example.clientExample.app.entities.FWEventResponse;
import com.example.clientExample.app.entities.FWEventView;
import com.example.clientExample.shared.FWAccessConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private FWEventResponse retrieveAllProjectEventsByCursor(String authToken, LocalDate from, LocalDate to, Integer cursor) {
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
        Set<String> objectIdSet = new HashSet<>(objectIds);
        Set<String> projectIdSet = new HashSet<>(projectIds);

        LocalDateTime rangeStart = from.atTime(3, 0);
        LocalDateTime rangeEnd = to.plusDays(1).atTime(2, 59, 59, 999_999_999);

        List<FWEvent> filteredEvents = new ArrayList<>();
        Integer currentCursor = 1;

        while (currentCursor != null && currentCursor != 0) {
            FWEventResponse response = retrieveAllProjectEventsByCursor(authToken, from, to.plusDays(1), currentCursor);

            // filter immediately, avoid storing unnecessary events
            response.events().stream()
                    .filter(e -> objectIdSet.isEmpty() || objectIdSet.contains(e.objectID()))
                    .filter(e -> projectIdSet.isEmpty() || projectIdSet.contains(e.projectID()))
                    .filter(e -> {
                        LocalDateTime ts = e.dateTimeInAsString();
                        return !ts.isBefore(rangeStart) && !ts.isAfter(rangeEnd);
                    })
                    .forEach(filteredEvents::add);

            currentCursor = response.nextCursor();
        }

        filteredEvents.sort(Comparator.comparing(FWEvent::dateTimeInAsString));

        return filteredEvents;
    }

    private String getCustomValue(List<FWCustoms> customs, String label) {
        return customs.stream()
                .filter(c -> label.equals(c.label()))
                .map(FWCustoms::value)
                .findFirst()
                .orElse("");
    }

    public List<FWEventView> retrieveProjectEventViews(LocalDate from, LocalDate to, List<String> objectIds, List<String> projectIds) {
        List<FWEvent> events = this.retrieveAllProjectEvents(from, to, objectIds, projectIds);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd   HH:mm");

        return events.stream()
                .map(ev -> {
                    String moderation = this.getCustomValue(ev.customs(), "Moderation");
                    String art = this.getCustomValue(ev.customs(), "Art");

                    return new FWEventView(ev.mainHeader(),
                            ev.projectName(),
                            ev.projectBinderName(),
                            ev.objectName(),
                            formatter.format(ev.dateTimeInAsString()),
                            formatter.format(ev.dateTimeOutAsString()),
                            ev.note(),
                            moderation,
                            art);
                }).toList();

    }
}
