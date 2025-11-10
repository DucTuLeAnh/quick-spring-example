package com.example.clientExample.app.service;

import com.example.clientExample.app.entities.ExampleResponse;
import org.springframework.stereotype.Service;

@Service
public class ExampleService {

    public ExampleResponse fetchExampleData() {
        // return new ExampleResponse(1, "Hans");
        return new ExampleResponse(1, "hans");
    }
}