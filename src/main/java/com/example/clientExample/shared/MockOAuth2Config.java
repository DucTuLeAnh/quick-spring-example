package com.example.clientExample.shared;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

@Configuration
@Profile("test")
public class MockOAuth2Config {

    @Bean("authorizedClientManager")
    public OAuth2AuthorizedClientManager authorizedClientManager() {
        // simple dummy implementation that never calls Azure
        return (request) -> null;
    }



}