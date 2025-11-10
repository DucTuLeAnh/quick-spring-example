package com.example.clientExample.shared;

import com.example.clientExample.app.entities.ExampleResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.client.RestClient;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

class OAuth2RestClientTest {

    @Test
    void fetchData_returnsDataSuccessfully() {

        ///////////////////// GIVEN

        // Mock OAuth2 token
        OAuth2AccessToken token = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "fake-token",
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );
        OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);
        when(authorizedClient.getAccessToken()).thenReturn(token);

        // Mock manager to return the fake client
        OAuth2AuthorizedClientManager clientManager = mock(OAuth2AuthorizedClientManager.class);
        when(clientManager.authorize(any())).thenReturn(authorizedClient);

        // ---- Mock RestClient fluent chain ----
        RestClient restClient = mock(RestClient.class);
        RestClient.RequestHeadersUriSpec<?> uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec<?> headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        doReturn(uriSpec).when(restClient).get();

        doReturn(headersSpec).when(uriSpec).uri(anyString());
        doReturn(headersSpec).when(headersSpec).header(anyString(), anyString());
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        ExampleResponse expected = new ExampleResponse(1, "Hank");
        when(responseSpec.body(ExampleResponse.class)).thenReturn(expected);

        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.build()).thenReturn(restClient);
        doReturn(restClient).when(builder).build();

        // Inject mocks into the service
        OAuth2RestClient oAuth2RestClient = new OAuth2RestClient(clientManager, builder);

        ///////////////////// WHEN
        ExampleResponse result = oAuth2RestClient.fetchData("https://api.example.com/data", ExampleResponse.class);

        ///////////////////// THEN
        assertEquals(new ExampleResponse(1, "Hank"), result);
        verify(clientManager).authorize(any());
        verify(restClient).get();
    }
}