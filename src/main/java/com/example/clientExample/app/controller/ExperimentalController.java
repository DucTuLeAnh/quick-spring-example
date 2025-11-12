package com.example.clientExample.app.controller;

import com.example.clientExample.app.entities.Event;
import com.example.clientExample.app.entities.FWObject;
import com.example.clientExample.app.service.EventQueryService;
import com.example.clientExample.app.service.FWObjectQueryService;
import com.example.clientExample.app.service.FwTokenService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ExperimentalController {
    private final FwTokenService fwTokenService;
    private final EventQueryService eventQueryService;
    private final FWObjectQueryService objectQueryService;

    public ExperimentalController(FwTokenService fwTokenService, EventQueryService eventQueryService, FWObjectQueryService objectQueryService) {
        this.fwTokenService = fwTokenService;
        this.eventQueryService = eventQueryService;
        this.objectQueryService = objectQueryService;
    }

    @GetMapping("/token")
    public String testo() {
        return "is it valid: ";
    }

    @GetMapping("/createtoken")
    public String createtoken() throws JsonProcessingException {
        return "is it valid: " + fwTokenService.getAuthToken();
    }
    @GetMapping("/testquery")
    public String testQuery() {
        List<FWObject> events = objectQueryService.retrieveAllRoomObjects();
        return "testquery: " + events.stream().map(FWObject::name).collect(Collectors.joining(","));
    }
}
