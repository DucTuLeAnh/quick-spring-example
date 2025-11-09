package com.example.clientExample.shared;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /*
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // public endpoints
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**", "/").permitAll()  // allow public endpoints
                        .anyRequest().authenticated()               // others require auth
                )
               // .csrf(AbstractHttpConfigurer::disable)  ;
                // enable OAuth2 login using the new DSL
                .oauth2Login(Customizer.withDefaults());

        return http.build();
    }
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // allow everything
                )
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}