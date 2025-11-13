package com.example.clientExample.app.controller;

import com.example.clientExample.app.entities.FWObject;
import com.example.clientExample.app.entities.FWProject;
import com.example.clientExample.app.service.FWEventQueryService;
import com.example.clientExample.app.service.FWObjectQueryService;
import com.example.clientExample.app.service.FWProjectQueryService;
import com.example.clientExample.app.service.FWTokenService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ExperimentalController {
    private final FWTokenService fwTokenService;
    private final FWEventQueryService FWEventQueryService;
    private final FWObjectQueryService objectQueryService;
    private final FWProjectQueryService projectQueryService;

    public ExperimentalController(FWTokenService fwTokenService, FWEventQueryService FWEventQueryService, FWObjectQueryService objectQueryService, FWProjectQueryService projectQueryService) {
        this.fwTokenService = fwTokenService;
        this.FWEventQueryService = FWEventQueryService;
        this.objectQueryService = objectQueryService;
        this.projectQueryService = projectQueryService;
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
        List<FWProject> projects = projectQueryService.retrieveAllRoomObjects();
        //return "testquery: " + projects.stream().map(FWProject::name).collect(Collectors.joining(","));
        return "testquery: " + projects.size();
    }
}
