package com.example.clientExample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class
})
public class ClientExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientExampleApplication.class, args);
	}

}
