package com.example.clientExample.app.controller;

import com.example.clientExample.app.entities.Event;
import com.example.clientExample.app.service.EventQueryService;
import com.example.clientExample.app.service.FwTokenService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ExperimentalController {
    private final FwTokenService fwTokenService;
    private final EventQueryService eventQueryService;

    public ExperimentalController(FwTokenService fwTokenService, EventQueryService eventQueryService) {
        this.fwTokenService = fwTokenService;
        this.eventQueryService = eventQueryService;
    }

    @GetMapping("/token")
    public String testo() {
        return "is it valid: " + fwTokenService.isTokenValid();
    }

    @GetMapping("/createtoken")
    public String createtoken() {
        return "is it valid: " + fwTokenService.getAuthToken();
    }
    @GetMapping("/testquery")
    public String testQuery() {

        List<Event> events = eventQueryService.retrieveAllEventsByDate("2025-11-10", "2025-11-11");
        return "testquery: " + events.size();
    }
}
