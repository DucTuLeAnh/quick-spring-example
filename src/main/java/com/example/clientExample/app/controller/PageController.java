package com.example.clientExample.app.controller;

import com.example.clientExample.app.entities.rest.FWObject;
import com.example.clientExample.app.entities.rest.FWProject;
import com.example.clientExample.app.entities.view.FWSearchResultEntryView;
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
import java.util.Collections;
import java.util.List;


/*


 - layout nebeneinander
 - start-date => end-date füllt sich automatisch
 - deutsches datumsformat,
 - deutsche texte

 */
@Controller
public class PageController {

    private final ExampleService exampleService;
    private final FWEventQueryService fwEventQueryService;
    private final FWObjectQueryService objectQueryService;
    private final FWProjectQueryService projectQueryService;


    public PageController(ExampleService service, FWEventQueryService fwEventQueryService, FWObjectQueryService objectQueryService, FWProjectQueryService projectQueryService) {
        this.exampleService = service;
        this.fwEventQueryService = fwEventQueryService;
        this.objectQueryService = objectQueryService;
        this.projectQueryService = projectQueryService;
    }

    @GetMapping("/start")
    public String testo(Model model) {
        model.addAttribute("allCategories", List.of("Test1", "Test2", "Test3", "Test4", "Test5", "Test6", "Test7", "Test8", "Test9", "Test2", "Test1", "Test2"));
        model.addAttribute("allStatuses", List.of("Status1", "Status2"));
        return "searchpage";
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
            @RequestParam(required = false) List<String> oids,
            @RequestParam(required = false) List<String> pids,
            Model model
    ) {

        System.out.println(startDate);
        System.out.println(endDate);
        System.out.println(oids);
        System.out.println(pids);
        List<FWObject> allRoomObjects = objectQueryService.retrieveAllRoomObjects();
        List<FWProject> projects = projectQueryService.retrieveAllProjects();
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("allObjects", allRoomObjects);
        model.addAttribute("allProjects", projects);



        //todo: think about where to do the null checks
        if (oids == null) {
            oids = Collections.emptyList();
        }

        if (pids == null) {
            pids = Collections.emptyList();
        }


        List<FWSearchResultEntryView> entryViews = fwEventQueryService.retrieveSearchResultEntryView(startDate, endDate, oids, pids, allRoomObjects);
        System.out.println("Size of the retrieved events: " + entryViews.size());
        model.addAttribute("results", entryViews);
        model.addAttribute("selectedPids", pids);
        model.addAttribute("selectedOids", oids);

        return "searchpage";
    }

    @GetMapping("/timeline")
    public String timeline(Model model) {
        LocalDate now = LocalDate.now();
        List<FWObject> allRoomObjects = objectQueryService.retrieveAllRoomObjects();
        List<FWSearchResultEntryView> entryViews = fwEventQueryService.retrieveTimelineViews(now, now, Collections.emptyList(), Collections.emptyList(), allRoomObjects);
        model.addAttribute("entries", entryViews);

        return "timeline";
    }
}
