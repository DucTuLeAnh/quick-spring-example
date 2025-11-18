package com.example.clientExample.shared;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TokenRefreshInterceptor implements ClientHttpRequestInterceptor {

    private final TokenHolder tokenHolder;
    private final TokenService tokenService;

    public TokenRefreshInterceptor(TokenHolder tokenHolder, TokenService tokenService) {
        this.tokenHolder = tokenHolder;
        this.tokenService = tokenService;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        // 1) Send the request with the current token
        request = new HttpRequestWrapper(request) {
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.putAll(super.getHeaders());
                headers.setBearerAuth(tokenHolder.get());
                return headers;
            }
        };

        ClientHttpResponse response = execution.execute(request, body);

        if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
            return response;
        }

        // 2) Token invalid → fetch a new one
        System.out.println("Generating new token!");
        String newToken = tokenService.getAuthToken();
        tokenHolder.set(newToken);

        // 3) Retry the original request with the fresh token
        HttpRequest retryRequest = new HttpRequestWrapper(request) {
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.putAll(super.getHeaders());
                headers.setBearerAuth(newToken);
                return headers;
            }
        };
        System.out.println("About to execute request");
        return execution.execute(retryRequest, body);
    }
}
