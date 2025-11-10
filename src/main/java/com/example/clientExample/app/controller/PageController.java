package com.example.clientExample.app.controller;

import com.example.clientExample.app.service.AppService;
import com.example.clientExample.app.service.ExampleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PageController {

    private final ExampleService exampleService;

    public PageController(ExampleService service) {
        this.exampleService = service;
    }

    @GetMapping("/start")
    public String testo() {
        return "testo"; // refers to users.html
    }

    @GetMapping("/view")
    public String listUsers(Model model) {
        model.addAttribute("examples", List.of(exampleService.fetchExampleData()));
        return "example"; // refers to users.html
    }
}
