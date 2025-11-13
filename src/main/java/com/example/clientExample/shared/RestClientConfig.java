package com.example.clientExample.shared;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.security.cert.X509Certificate;


@Configuration
public class RestClientConfig {


    @Bean
    @Profile("test")
    public RestClient restClient(RestClient.Builder restClientBuilder) throws Exception {
        return restClientBuilder.requestInterceptor((request, body, execution) -> {
            long start = System.currentTimeMillis();
            try {
                System.out.printf("➡️ Sending %s %s%n", request.getMethod(), request.getURI());
                // Log request headers
                request.getHeaders().forEach((key, values) ->
                        System.out.printf("   %s: %s%n", key, String.join(", ", values))
                );

                var response = execution.execute(request, body);
                long duration = System.currentTimeMillis() - start;
                System.out.printf("✅ Response: %d (%d ms)%n", response.getStatusCode().value(), duration);
                return response;
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - start;
                System.out.printf("❌ Exception after %d ms: %s%n", duration, e.toString());
                e.printStackTrace();
                throw e;
            }
        }).build();
    }


    @Bean
    @Profile("asd")
    public RestClient test(RestClient.Builder restClientBuilder) throws Exception {
        return restClientBuilder.build();
    }


    @Bean
    @Profile("bob")
    public RestClient unsecureRestClient(RestClient.Builder restClientBuilder) throws Exception {
        // Trust all certificates
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Install the all-trusting trust manager
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        // Disable hostname verification
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        // Use simple request factory (uses HttpURLConnection under the hood)
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        // --- Logging interceptor ---


        return restClientBuilder
                .requestFactory(factory)
                .requestInterceptor((request, body, execution) -> {
                    long start = System.currentTimeMillis();
                    try {
                        System.out.printf("➡️ Sending %s %s%n", request.getMethod(), request.getURI());
                        var response = execution.execute(request, body);
                        long duration = System.currentTimeMillis() - start;
                        System.out.printf("✅ Response: %d (%d ms)%n", response.getStatusCode().value(), duration);
                        return response;
                    } catch (Exception e) {
                        long duration = System.currentTimeMillis() - start;
                        System.out.printf("❌ Exception after %d ms: %s%n", duration, e.toString());
                        e.printStackTrace();
                        throw e;
                    }
                })
                .build();
    }
}