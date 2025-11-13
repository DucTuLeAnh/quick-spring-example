package com.example.clientExample.app.controller;

import com.example.clientExample.app.entities.FWObject;
import com.example.clientExample.app.entities.FWProject;
import com.example.clientExample.app.service.ExampleService;
import com.example.clientExample.app.service.FWEventQueryService;
import com.example.clientExample.app.service.FWObjectQueryService;
import com.example.clientExample.app.service.FWProjectQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

@Controller
public class PageController {

    private final ExampleService exampleService;
    private final FWEventQueryService FWEventQueryService;
    private final FWObjectQueryService objectQueryService;
    private final FWProjectQueryService projectQueryService;


    public PageController(ExampleService service, FWEventQueryService fwEventQueryService, FWObjectQueryService objectQueryService, FWProjectQueryService projectQueryService) {
        this.exampleService = service;
        FWEventQueryService = fwEventQueryService;
        this.objectQueryService = objectQueryService;
        this.projectQueryService = projectQueryService;
    }

    @GetMapping("/start")
    public String testo(Model model) {
        model.addAttribute("allCategories", List.of("Test1", "Test2","Test3", "Test4","Test5", "Test6","Test7", "Test8","Test9", "Test2","Test1", "Test2"));
        model.addAttribute("allStatuses", List.of("Status1", "Status2"));
        return "searchpage"; // refers to users.html
    }

    /*
    @GetMapping("/view")
    public String listUsers(Model model) {
        model.addAttribute("examples", List.of(exampleService.fetchExampleData()));
        return "example"; // refers to users.html
    }
*/
    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model
    ) {
        List<FWObject> objects = objectQueryService.retrieveAllRoomObjects();
        List<FWProject> projects = projectQueryService.retrieveAllRoomObjects();
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("allCategories", objects.stream().map(FWObject::name));
        model.addAttribute("allStatuses", projects.stream().map(FWProject::name));
        if (startDate != null && endDate != null) {
            // Dummy data example – replace with your actual query logic
            List<String> results = new LinkedList<>(

            );
            for(int i = 0; i< 55; i++){
                results.add("Some other string");
            }
            model.addAttribute("results", results);
        }

        return "searchpage";
    }
}
