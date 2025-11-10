package com.example.clientExample.app.service;

import com.example.clientExample.app.entities.ExampleResponse;
import com.example.clientExample.shared.OAuth2RestClient;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppServiceTest {

    @Test
    void fetchData_returnsDataSuccessfully() {
        ////////////////////// GIVEN
        OAuth2RestClient mockClient = mock(OAuth2RestClient.class);
        when(mockClient.fetchData("http://example-data.com", ExampleResponse.class)).thenReturn(new ExampleResponse(1, "Test"));

        AppService appService = new AppService(mockClient);

        ////////////////////// WHEN
        ExampleResponse result = appService.fetchExampleData();

        ////////////////////// THEN
        assertEquals(new ExampleResponse(1, "Test"), result);
    }
}
