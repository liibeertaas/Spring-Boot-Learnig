package com.example.geo_places_service.client;

import com.example.geo_places_service.dto.GeoCoordinates;
import com.example.geo_places_service.exception.GeocodingUnavailableException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;

/**
 * Тонка обгортка над Nominatim REST API (/search, /reverse).
 * Правила чемного використання Nominatim:
 *  - власний User-Agent (див. {@link com.example.geo_places_service.config.RestClientConfig});
 *  - не більше 1 запиту/сек — тут це гарантується синхронізованим тротлінгом
 *    (для навчального проєкту з одним інстансом застосунку цього достатньо;
 *    у розподіленому середовищі знадобився б зовнішній rate-limiter).
 */
@Component
public class NominatimClient {

    private static final long MIN_INTERVAL_MILLIS = 1000L;

    private final RestClient restClient;
    private final Object throttleLock = new Object();
    private volatile long lastRequestAtMillis = 0L;

    public NominatimClient(@Qualifier("nominatimRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public Optional<GeoCoordinates> search(String address) {
        throttle();
        try {
            List<NominatimSearchResult> results = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/search")
                            .queryParam("q", address)
                            .queryParam("format", "json")
                            .queryParam("limit", 1)
                            .build())
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<NominatimSearchResult>>() {
                    });

            if (results == null || results.isEmpty()) {
                return Optional.empty();
            }
            NominatimSearchResult first = results.get(0);
            return Optional.of(new GeoCoordinates(
                    Double.parseDouble(first.lat()),
                    Double.parseDouble(first.lon())));
        } catch (RestClientException ex) {
            throw new GeocodingUnavailableException(
                    "Nominatim недоступний під час геокодування адреси \"%s\"".formatted(address), ex);
        }
    }

    public Optional<String> reverse(double latitude, double longitude) {
        throttle();
        try {
            NominatimReverseResult result = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/reverse")
                            .queryParam("lat", latitude)
                            .queryParam("lon", longitude)
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .body(NominatimReverseResult.class);

            return Optional.ofNullable(result).map(NominatimReverseResult::display_name);
        } catch (RestClientException ex) {
            throw new GeocodingUnavailableException(
                    "Nominatim недоступний під час зворотного геокодування (%s, %s)".formatted(latitude, longitude), ex);
        }
    }

    private void throttle() {
        synchronized (throttleLock) {
            long elapsed = System.currentTimeMillis() - lastRequestAtMillis;
            if (elapsed < MIN_INTERVAL_MILLIS) {
                try {
                    Thread.sleep(MIN_INTERVAL_MILLIS - elapsed);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            lastRequestAtMillis = System.currentTimeMillis();
        }
    }
}
