package com.example.clientExample.app.controller;

import com.example.clientExample.app.entities.rest.FWObject;
import com.example.clientExample.app.entities.rest.FWProject;
import com.example.clientExample.app.service.FWEventQueryService;
import com.example.clientExample.app.service.FWObjectQueryService;
import com.example.clientExample.app.service.FWProjectQueryService;
import com.example.clientExample.app.service.FWTokenService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ExperimentalController {
    private final FWTokenService fwTokenService;
    private final FWEventQueryService fwEventQueryService;
    private final FWObjectQueryService objectQueryService;
    private final FWProjectQueryService projectQueryService;

    public ExperimentalController(FWTokenService fwTokenService, FWEventQueryService fwEventQueryService, FWObjectQueryService objectQueryService, FWProjectQueryService projectQueryService) {
        this.fwTokenService = fwTokenService;
        this.fwEventQueryService = fwEventQueryService;
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
        List<FWObject> objects = objectQueryService.retrieveAllRoomObjects();
        List<FWProject> projects = projectQueryService.retrieveAllRoomObjects();
        //return "testquery: " + projects.stream().map(FWProject::name).collect(Collectors.joining(","));
        return "testquery: " + projects.size();
    }
}
