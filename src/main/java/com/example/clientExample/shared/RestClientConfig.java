package com.example.clientExample.shared;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;


@Configuration
public class RestClientConfig {

    /**
     * Unfortunately the FW API does not implement oAuth OR has a validity field in it's token.
     * That means, we have to implement a {@link TokenRefreshInterceptor} that during a 401, refetches a token and adds that to the Bearer header.
     *
     * However the client that fetches the token, must be another client than the one that gets intercepted with the {@link TokenRefreshInterceptor}.
     * otherwise we get a circular dependency.
     *
     * So this client here, IS ONLY USED TO FETCH THE TOKENS!
     */
    @Bean
    @Qualifier("tokenClient")
    public RestClient tokenClient() {
        // 1️⃣ Build RequestConfig with timeouts
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(5)) // time to get connection from pool
                //.setConnectTimeout(Timeout.ofSeconds(5))           // TCP handshake
                .setResponseTimeout(Timeout.ofSeconds(5))          // wait for server response
                .build();

        // 2️⃣ Build HttpClient using the RequestConfig
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .build();

        // 3️⃣ Create Spring RestClient
        return RestClient.builder()
                /* For some reason, on first token fetch, the tcp connection simply times out. If we force the execution with this,
                 *  the request simply gets through.
                 */
                .requestInterceptor((request, body, execution) -> execution.execute(request, body))
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
                .build();
    }

    @Bean
    @Profile("test")
    public RestClient restClient(RestClient.Builder restClientBuilder, TokenRefreshInterceptor interceptor) throws Exception {
        return restClientBuilder.
                requestInterceptor(interceptor)
                .requestInterceptor((request, body, execution) -> {
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
                })

                .build();
    }


    @Bean
    @Profile("asd")
    public RestClient test(RestClient.Builder restClientBuilder) throws Exception {
        return restClientBuilder.build();
    }


    @Bean
    @Profile("bob")
    public RestClient unsecureRestClient(RestClient.Builder restClientBuilder, TokenRefreshInterceptor interceptor) throws Exception {
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
                .requestInterceptor(interceptor)
                .build();
    }
}