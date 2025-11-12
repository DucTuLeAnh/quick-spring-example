package com.example.clientExample;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;

@SpringBootApplication
public class ClientExampleApplication {

	public static void main(String[] args) throws Exception {

        SpringApplication.run(ClientExampleApplication.class, args);
	}



}
