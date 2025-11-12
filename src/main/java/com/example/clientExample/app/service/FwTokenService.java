package com.example.clientExample.app.service;

import com.example.clientExample.app.entities.TokenActiveResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@Scope("application")
public class FwTokenService {


    private RestClient restClient;
    private String token = "";

    public FwTokenService(RestClient restClient) {
        this.restClient = restClient;
    }

    public String getDebugToken(){
        return this.token;
    }


}
