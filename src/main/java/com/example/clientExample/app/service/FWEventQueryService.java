package com.example.clientExample.app.service;

import com.example.clientExample.app.entities.rest.FWCustoms;
import com.example.clientExample.app.entities.rest.FWEvent;
import com.example.clientExample.app.entities.rest.FWEventResponse;
import com.example.clientExample.app.entities.rest.FWObject;
import com.example.clientExample.app.entities.view.FWEventView;
import com.example.clientExample.app.entities.view.FWSearchResultEntryKey;
import com.example.clientExample.app.entities.view.FWSearchResultEntryView;
import com.example.clientExample.shared.FWAccessConfiguration;
import com.example.clientExample.shared.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class FWEventQueryService {

    public static final String PROJECT_EVENT_TYPE = "project";
    private final RestClient restClient;
    private final TokenService tokenService;
    private final FWAccessConfiguration config;
    private final FWObjectQueryService objectQueryService;

    public FWEventQueryService(RestClient restClient, TokenService tokenService, FWAccessConfiguration config, FWObjectQueryService objectQueryService) {
        this.restClient = restClient;
        this.tokenService = tokenService;
        this.config = config;
        this.objectQueryService = objectQueryService;
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
                //.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                //.header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .retrieve()
                .body(FWEventResponse.class);

        return Optional.ofNullable(response).orElseThrow(() -> new IllegalStateException("Null object received in FW Project Query Service."));
    }


    public List<FWEvent> retrieveAllProjectEventsAccordingToBroadcastTime(LocalDate from, LocalDate to, List<String> objectIds, List<String> projectIds) {
        if (from == null || to == null) {
            return Collections.emptyList();
        }

        //String authToken = this.fwTokenService.getAuthToken();
        List<FWEvent> filteredEvents = new ArrayList<>();
        Integer currentCursor = 1;

        while (currentCursor != null && currentCursor != 0) {
            // A broadcast day goes from 3:00 - 2:59, so we need to consider the next day as well
            FWEventResponse response = retrieveAllProjectEventsByCursor("", from, to.plusDays(1), currentCursor);
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

    public List<FWSearchResultEntryView> retrieveTimelineViews(LocalDate from, LocalDate to, List<String> objectIds, List<String> projectIds, List<FWObject> allRoomObjects) {
        List<FWSearchResultEntryView> views = retrieveSearchResultEntryView(from,to,objectIds,projectIds,allRoomObjects);

        LocalDateTime now = LocalDateTime.now();
        return IntStream.range(0, views.size())
                .filter(i -> {
                    LocalDateTime end = (i + 1 < views.size()) ? views.get(i + 1).sortTime() : null;
                    return end == null || !now.isAfter(end); // keep if current time before next timestamp
                })
                .mapToObj(views::get)
                .toList();

    }

    public List<FWSearchResultEntryView> retrieveSearchResultEntryView(LocalDate from, LocalDate to, List<String> objectIds, List<String> projectIds, List<FWObject> allRoomObjects) {
        List<FWEvent> eventsByBroadcastTime = this.retrieveAllProjectEventsAccordingToBroadcastTime(from, to, objectIds, projectIds);


        Map<FWSearchResultEntryKey, List<FWEvent>> groupedBySearchResultEntryKey =
                eventsByBroadcastTime.stream()
                        .collect(Collectors.groupingBy(
                                p -> new FWSearchResultEntryKey(p.projectID(), p.bookingNumber(), p.projectBinderID())
                        ));

        List<FWSearchResultEntryView> views = this.mapToFWSearchResultEntryViews(groupedBySearchResultEntryKey, allRoomObjects);

        highlightFirstMatchingView(views);

        Set<String> projectIdSet = new HashSet<>(projectIds);
        return views.stream()
                .filter(view ->
                        (projectIdSet.isEmpty() || view.projectIds().stream().anyMatch(projectIdSet::contains))
                        && (objectIds.isEmpty() || view.objectIds().containsAll(objectIds))
                )
                .toList();

    }

    private void highlightFirstMatchingView(List<FWSearchResultEntryView> views) {
        LocalDateTime now = LocalDateTime.now();
        Optional<Integer> opt = IntStream.range(0, views.size())
                .filter(i -> !views.get(i).sortTime().isAfter(now))
                .boxed()
                .max(Comparator.comparing(i -> views.get(i).sortTime()));

        opt.ifPresent(i -> {
            FWSearchResultEntryView old = views.get(i);
            FWSearchResultEntryView updated = new FWSearchResultEntryView(
                    old.mainKey(),
                    old.entryHeader(),
                    old.projectIds(),
                    old.objectIds(),
                    old.objectNames(),
                    old.timeInAsString(),
                    old.timeOutAsString(),
                    old.dateInAsString(),
                    old.dateOutAsString(),
                    old.sortTime(),
                    old.notes(),
                    old.customModerators(),
                    old.customArts(),
                    true

            );
            views.set(i, updated);
        });
    }

    private List<FWSearchResultEntryView> mapToFWSearchResultEntryViews(Map<FWSearchResultEntryKey, List<FWEvent>> grouped, List<FWObject> allRoomObjects) {
        List<FWSearchResultEntryView> searchResultEntryViews = new ArrayList<>();

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        for (Map.Entry<FWSearchResultEntryKey, List<FWEvent>> entry : grouped.entrySet()) {

            FWSearchResultEntryKey key = entry.getKey();

            List<FWEvent> allFwEvents = entry.getValue();

            Set<String> allRoomObjectIds = allRoomObjects.stream().map(FWObject::objectID).collect(Collectors.toSet());


            /**
             * Unfortunately the API doesnt provide the booking Title only! It's only combined with the mainHeader, which contains unnecessary information like the booked object class.
             * However the Booking-Title is ALWAYS only the last element after the dot. So we retrieve it sneaky from the mainHeader with this logic.
             */

            final String bookingTitle = allFwEvents.stream()
                    .findAny()
                    .map(FWEvent::mainHeader)
                    .map(mainHeader -> {
                        int lastDot = mainHeader.lastIndexOf("•");
                        if (lastDot != -1) {
                            // Extract substring after the last dot, and trim whitespace
                            return mainHeader.substring(lastDot + 1).trim();
                        }
                        return mainHeader;
                    })
                    .orElse("");

            String entryHeader = allFwEvents.stream()
                    .findAny()
                    .map(fwEvent -> fwEvent.projectName() + " • " + fwEvent.projectBinderName() + " • " + bookingTitle)
                    .orElse("");




            List<String> objectNames = allFwEvents.stream()
                    .filter(event -> allRoomObjectIds.contains(event.objectID()))
                    .map(FWEvent::objectName)
                    .filter(s -> !s.isBlank())
                    .toList();

            Set<String> objectIds = allFwEvents.stream().map(FWEvent::objectID).filter(s -> !s.isBlank()).collect(Collectors.toSet());

            Set<String> projectIds = allFwEvents.stream().map(FWEvent::projectID).filter(s -> !s.isBlank()).collect(Collectors.toSet());

            LocalDateTime sortTime = allFwEvents.stream()
                    .findAny()
                    .map(FWEvent::dateTimeInAsString)
                    .orElseThrow(() -> new IllegalStateException("The date time in must not be null!"));

            String timeInAsString = allFwEvents.stream()
                    .findAny()
                    .map(FWEvent::dateTimeInAsString)
                    .map(timeFormatter::format)
                    .orElseThrow(() -> new IllegalStateException("The date time in must not be null!"));

            String timeOutAsString = allFwEvents.stream()
                    .findAny()
                    .map(FWEvent::dateTimeOutAsString)
                    .map(timeFormatter::format)
                    .orElseThrow(() -> new IllegalStateException("The date time out must not be null!"));

            String dateInAsString = allFwEvents.stream()
                    .findAny()
                    .map(FWEvent::dateTimeInAsString)
                    .map(dateFormatter::format)
                    .orElseThrow(() -> new IllegalStateException("The date time in must not be null!"));

            String dateOutAsString = allFwEvents.stream()
                    .findAny()
                    .map(FWEvent::dateTimeOutAsString)
                    .map(dateFormatter::format)
                    .orElseThrow(() -> new IllegalStateException("The date time out must not be null!"));

            List<String> notes = allFwEvents.stream().map(FWEvent::note).filter(s -> !s.isBlank()).toList();

            List<String> customModerators = allFwEvents.stream()
                    .flatMap(e -> e.customs().stream())
                    .filter(c -> "Moderation".equals(c.label()) && !c.value().isBlank())
                    .map(FWCustoms::value)
                    .distinct()
                    .toList();

            List<String> customArts = allFwEvents.stream()
                    .flatMap(e -> e.customs().stream())
                    .filter(c -> "Art".equals(c.label()) && !c.value().isBlank())
                    .map(FWCustoms::value)
                    .distinct()
                    .toList();

            searchResultEntryViews.add(new FWSearchResultEntryView(key, entryHeader, projectIds, objectIds, objectNames, timeInAsString, timeOutAsString,dateInAsString, dateOutAsString, sortTime, notes, customModerators, customArts, false));
        }

        searchResultEntryViews.sort(Comparator.comparing(FWSearchResultEntryView::sortTime));

        return searchResultEntryViews;
    }
}
