package com.example.geo_places_service.service;

import com.example.geo_places_service.client.NominatimClient;
import com.example.geo_places_service.dto.GeoCoordinates;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Перевіряє вимогу ПЗ-4: та сама адреса не повинна йти в Nominatim двічі.
 * Піднімає повний Spring-контекст (щоб спрацював @Cacheable-проксі),
 * а сам мережевий клієнт замінено моком.
 */
@SpringBootTest
class GeocodingCacheTest {

    @Autowired
    private GeocodingService geocodingService;

    @MockitoBean
    private NominatimClient nominatimClient;

    @Test
    void secondGeocodeCallForSameAddressDoesNotHitNominatimAgain() {
        String address = "Rynok Square 1, Lviv";
        when(nominatimClient.search(address)).thenReturn(Optional.of(new GeoCoordinates(49.8419, 24.0315)));

        GeoCoordinates first = geocodingService.geocode(address);
        GeoCoordinates second = geocodingService.geocode(address);

        assertThat(first).isEqualTo(second);
        verify(nominatimClient, times(1)).search(address);
    }

    @Test
    void differentAddressesEachHitNominatimOnce() {
        when(nominatimClient.search("Address A")).thenReturn(Optional.of(new GeoCoordinates(1.0, 1.0)));
        when(nominatimClient.search("Address B")).thenReturn(Optional.of(new GeoCoordinates(2.0, 2.0)));

        geocodingService.geocode("Address A");
        geocodingService.geocode("Address B");
        geocodingService.geocode("Address A");

        verify(nominatimClient, times(1)).search("Address A");
        verify(nominatimClient, times(1)).search("Address B");
    }
}
