package com.example.clientExample.app.service;

import com.example.clientExample.app.entities.rest.FWCustoms;
import com.example.clientExample.app.entities.rest.FWEvent;
import com.example.clientExample.app.entities.rest.FWEventResponse;
import com.example.clientExample.app.entities.view.FWEventView;
import com.example.clientExample.app.entities.view.FWSearchResultEntryKey;
import com.example.clientExample.app.entities.view.FWSearchResultEntryView;
import com.example.clientExample.shared.FWAccessConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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


    public List<FWEvent> retrieveAllProjectEventsAccordingToBroadcastTime(LocalDate from, LocalDate to, List<String> objectIds, List<String> projectIds) {
        if (from == null || to == null) {
            return Collections.emptyList();
        }

        String authToken = this.fwTokenService.getAuthToken();
        List<FWEvent> filteredEvents = new ArrayList<>();
        Integer currentCursor = 1;

        while (currentCursor != null && currentCursor != 0) {
            // A broadcast day goes from 3:00 - 2:59, so we need to consider the next day as well
            FWEventResponse response = retrieveAllProjectEventsByCursor(authToken, from, to.plusDays(1), currentCursor);
            filteredEvents.addAll(this.filterByBroadcastTime(response.events(), from, to));
            currentCursor = response.nextCursor();
        }

        filteredEvents.sort(Comparator.comparing(FWEvent::dateTimeInAsString));

        return filteredEvents;
    }

    /**
     * A broadcast day starts at 3 A.M and ends at 2:59 AM on the next day.
     * So all events on a starting day before 3 A.M must be thrown out and all events on the nex day till 2:59 must be included.
     *
     */
    private List<FWEvent> filterByBroadcastTime(List<FWEvent> allEvents, LocalDate from, LocalDate to) {
        LocalDateTime rangeStart = from.atTime(3, 0);
        LocalDateTime rangeEnd = to.plusDays(1).atTime(2, 59, 59, 999_999_999);
        return allEvents.stream()
                .filter(e -> {
                    LocalDateTime ts = e.dateTimeInAsString();
                    return !ts.isBefore(rangeStart) && !ts.isAfter(rangeEnd);
                })
                .toList();
    }

    private List<FWEvent> filterByObjectId(List<FWEvent> allEvents, List<String> objectIds) {
        Set<String> objectIdSet = new HashSet<>(objectIds);
        return allEvents.stream()
                .filter(e ->
                        objectIdSet.isEmpty() || objectIdSet.contains(e.objectID())
                )
                .toList();
    }

    private List<FWEvent> filterByProjectId(List<FWEvent> allEvents, List<String> projectIds) {
        Set<String> projectIdSet = new HashSet<>(projectIds);
        return allEvents.stream()
                .filter(e ->
                        projectIdSet.isEmpty() || projectIdSet.contains(e.objectID())
                )
                .toList();
    }

    private String getCustomValue(List<FWCustoms> customs, String label) {
        return customs.stream()
                .filter(c -> label.equals(c.label()))
                .map(FWCustoms::value)
                .findFirst()
                .orElse("");
    }

    public List<FWEventView> retrieveProjectEventViews(LocalDate from, LocalDate to, List<String> objectIds, List<String> projectIds) {
        List<FWEvent> eventsByBroadcastTime = this.retrieveAllProjectEventsAccordingToBroadcastTime(from, to, objectIds, projectIds);


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd   HH:mm");

        return eventsByBroadcastTime.stream()
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


    public List<FWSearchResultEntryView> retrieveSearchResultEntryView(LocalDate from, LocalDate to, List<String> objectIds, List<String> projectIds) {
        List<FWEvent> eventsByBroadcastTime = this.retrieveAllProjectEventsAccordingToBroadcastTime(from, to, objectIds, projectIds);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        Map<FWSearchResultEntryKey, List<FWEvent>> groupedBySearchResultEntryKey =
                eventsByBroadcastTime.stream()
                        .collect(Collectors.groupingBy(
                                p -> new FWSearchResultEntryKey(p.projectID(), p.bookingNumber(), p.projectBinderID())
                        ));

        List<FWSearchResultEntryView> views = this.mapToFWSearchResultEntryViews(groupedBySearchResultEntryKey, formatter);

        Set<String> projectIdSet = new HashSet<>(projectIds);
        return views.stream()
                .filter(view ->
                        (projectIdSet.isEmpty() || view.projectIds().stream().anyMatch(projectIdSet::contains))
                        && (objectIds.isEmpty() || view.objectIds().containsAll(objectIds))
                )
                .toList();

    }

    private List<FWSearchResultEntryView> mapToFWSearchResultEntryViews(Map<FWSearchResultEntryKey, List<FWEvent>> grouped, DateTimeFormatter formatter) {
        List<FWSearchResultEntryView> searchResultEntryViews = new ArrayList<>();

        for (Map.Entry<FWSearchResultEntryKey, List<FWEvent>> entry : grouped.entrySet()) {

            FWSearchResultEntryKey key = entry.getKey();

            List<FWEvent> allFwEvents = entry.getValue();

            String entryHeader = allFwEvents.stream()
                    .findAny()
                    .map(groupedEvent -> groupedEvent.projectName() + " || " + groupedEvent.projectName() + " || " + groupedEvent.mainHeader())
                    .orElse("");

            List<String> objectNames = allFwEvents.stream().map(FWEvent::objectName).filter(s -> !s.isBlank()).toList();
            Set<String> objectIds = allFwEvents.stream().map(FWEvent::objectID).filter(s -> !s.isBlank()).collect(Collectors.toSet());
            Set<String> projectIds = allFwEvents.stream().map(FWEvent::projectID).filter(s -> !s.isBlank()).collect(Collectors.toSet());

            LocalDateTime sortTime = allFwEvents.stream()
                    .findAny()
                    .map(FWEvent::dateTimeInAsString)
                    .orElseThrow(() -> new IllegalStateException("The date time in must not be null!"));

            String dateTimeInAsString = allFwEvents.stream()
                    .findAny()
                    .map(FWEvent::dateTimeInAsString)
                    .map(formatter::format)
                    .orElseThrow(() -> new IllegalStateException("The date time in must not be null!"));

            String dateTimeOutAsString = allFwEvents.stream()
                    .findAny()
                    .map(FWEvent::dateTimeOutAsString)
                    .map(formatter::format)
                    .orElseThrow(() -> new IllegalStateException("The date time out must not be null!"));

            List<String> notes = allFwEvents.stream().map(FWEvent::note).filter(s -> !s.isBlank()).toList();

            List<String> customModerators = allFwEvents.stream().map(FWEvent::note).filter(s -> !s.isBlank()).toList();

            List<String> customArts = allFwEvents.stream().map(FWEvent::note).filter(s -> !s.isBlank()).toList();

            searchResultEntryViews.add(new FWSearchResultEntryView(key, entryHeader, projectIds, objectIds, objectNames, dateTimeInAsString, dateTimeOutAsString, sortTime, notes, customModerators, customArts));
        }

        searchResultEntryViews.sort(Comparator.comparing(FWSearchResultEntryView::sortTime));

        return searchResultEntryViews;
    }
}
