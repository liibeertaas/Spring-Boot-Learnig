package com.example.geo_places_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * HTTP-клієнт для Nominatim (OpenStreetMap).
 * Обрано RestClient (Spring 6.1+): синхронний, без реактивного стеку (нам достатньо,
 * бо гео-запити все одно серіалізуються rate-лімітом у {@link com.example.geo_places_service.client.NominatimClient}),
 * простіший за WebClient і сучасніший за RestTemplate.
 */
@Configuration
public class RestClientConfig {

    // Nominatim вимагає власний, розпізнаваний User-Agent (Usage Policy).
    // Значення можна перевизначити через nominatim.user-agent у application.yml / env.
    @Value("${nominatim.user-agent:geo-places-service-vlasenko-edu/1.0}")
    private String nominatimUserAgent;

    @Value("${nominatim.base-url:https://nominatim.openstreetmap.org}")
    private String nominatimBaseUrl;

    @Bean
    public RestClient nominatimRestClient() {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build());
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .baseUrl(nominatimBaseUrl)
                .defaultHeader(HttpHeaders.USER_AGENT, nominatimUserAgent)
                .requestFactory(requestFactory)
                .build();
    }
}
