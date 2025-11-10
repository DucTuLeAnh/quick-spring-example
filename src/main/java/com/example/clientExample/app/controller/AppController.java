package com.example.clientExample.app.controller;

import com.example.clientExample.app.entities.ExampleResponse;
import com.example.clientExample.app.service.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

@RestController
public class AppController {
    private final AppService appService;

    @Autowired
    public AppController(AppService appService) {
        this.appService = appService;
    }

    @GetMapping("/api/data")
    public ExampleResponse getData() {
        return appService.fetchExampleData();
    }

    @GetMapping("/public")
    public String publicEndpoint() {
        return "public";
    }

    @GetMapping("/private")
    public String privateEndpoint(Authentication authentication) {

        if(authentication == null){
            return "Didnt work";
        }
        BearerTokenAuthentication auth = (BearerTokenAuthentication) authentication;

        OAuth2IntrospectionAuthenticatedPrincipal principal =
                (OAuth2IntrospectionAuthenticatedPrincipal) authentication.getPrincipal();

        return "Hello " + auth.getName() + ", principal=" + principal.getName();
    }
}
