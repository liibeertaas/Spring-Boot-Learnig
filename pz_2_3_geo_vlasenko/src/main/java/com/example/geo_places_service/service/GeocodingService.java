package com.example.geo_places_service.service;

import com.example.geo_places_service.client.NominatimClient;
import com.example.geo_places_service.config.CacheConfig;
import com.example.geo_places_service.dto.GeoCoordinates;
import com.example.geo_places_service.exception.AddressNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Доменний шар над {@link NominatimClient}: адреса <-> координати.
 * Обидва методи кешовані ({@link CacheConfig}) — та сама адреса/пара координат
 * не йде в Nominatim повторно. Винятки (адресу не знайдено/сервіс недоступний)
 * НЕ кешуються Spring Cache за замовчуванням, тож невдалий запит можна повторити пізніше.
 */
@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final NominatimClient nominatimClient;

    @Cacheable(CacheConfig.GEOCODE_CACHE)
    public GeoCoordinates geocode(String address) {
        return nominatimClient.search(address)
                .orElseThrow(() -> new AddressNotFoundException(address));
    }

    @Cacheable(CacheConfig.REVERSE_GEOCODE_CACHE)
    public String reverseGeocode(double latitude, double longitude) {
        return nominatimClient.reverse(latitude, longitude)
                .orElseThrow(() -> new AddressNotFoundException("%s, %s".formatted(latitude, longitude)));
    }
}
