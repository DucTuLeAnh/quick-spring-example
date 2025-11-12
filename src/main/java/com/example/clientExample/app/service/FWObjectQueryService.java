package com.example.clientExample.app.service;


import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


@Service
public class FWObjectQueryService {
    private final RestClient restClient;
    private final FwTokenService fwTokenService;

    public FWObjectQueryService(RestClient restClient, FwTokenService fwTokenService) {
        this.restClient = restClient;
        this.fwTokenService = fwTokenService;
    }

    public String testo() {

        return "";
    }
}
