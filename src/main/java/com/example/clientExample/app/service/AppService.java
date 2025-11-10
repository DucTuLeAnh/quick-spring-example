package com.example.clientExample.app.service;

import com.example.clientExample.app.entities.ExampleResponse;
import com.example.clientExample.shared.OAuth2RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AppService {

    private final OAuth2RestClient oAuth2RestClient;

    @Autowired
    public AppService(OAuth2RestClient oAuth2RestClient) {
        this.oAuth2RestClient = oAuth2RestClient;
    }

    public ExampleResponse fetchExampleData() {
       // return new ExampleResponse(1, "Hans");
        return oAuth2RestClient.fetchData("http://example-data.com", ExampleResponse.class);
    }
}
