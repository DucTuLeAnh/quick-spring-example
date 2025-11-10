package com.example.clientExample.shared;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("test")
public class TestSecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // allow everything
                )
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    // Optional: provide a mock Authentication for testing
    @Bean
    @Profile("test")
    public AuthenticationManager authenticationManager() {
        return authentication -> {
            authentication.setAuthenticated(true);
            return authentication;
        };
    }
}
