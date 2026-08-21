package com.example.geo_places_service.service;

import com.example.geo_places_service.client.NominatimClient;
import com.example.geo_places_service.dto.GeoCoordinates;
import com.example.geo_places_service.exception.AddressNotFoundException;
import com.example.geo_places_service.exception.GeocodingUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Юніт-тест доменного шару геокодування з мок-гео-клієнтом (без реального звернення
 * до Nominatim) — сценарії: знайдено / не знайдено / гео-сервіс недоступний.
 */
@ExtendWith(MockitoExtension.class)
class GeocodingServiceTest {

    @Mock
    private NominatimClient nominatimClient;

    @Test
    void geocodeReturnsCoordinatesWhenAddressFound() {
        when(nominatimClient.search("Rynok Square 1, Lviv"))
                .thenReturn(Optional.of(new GeoCoordinates(49.8419, 24.0315)));

        GeocodingService service = new GeocodingService(nominatimClient);
        GeoCoordinates result = service.geocode("Rynok Square 1, Lviv");

        assertThat(result.latitude()).isEqualTo(49.8419);
        assertThat(result.longitude()).isEqualTo(24.0315);
    }

    @Test
    void geocodeThrowsAddressNotFoundWhenNominatimReturnsNothing() {
        when(nominatimClient.search("nonexistent address 999"))
                .thenReturn(Optional.empty());

        GeocodingService service = new GeocodingService(nominatimClient);

        assertThatThrownBy(() -> service.geocode("nonexistent address 999"))
                .isInstanceOf(AddressNotFoundException.class);
    }

    @Test
    void geocodePropagatesUnavailableExceptionInsteadOfCrashingWith500() {
        when(nominatimClient.search("any address"))
                .thenThrow(new GeocodingUnavailableException("timeout", new RuntimeException("timeout")));

        GeocodingService service = new GeocodingService(nominatimClient);

        assertThatThrownBy(() -> service.geocode("any address"))
                .isInstanceOf(GeocodingUnavailableException.class);
    }

    @Test
    void reverseGeocodeReturnsAddressWhenFound() {
        when(nominatimClient.reverse(48.8584, 2.2945))
                .thenReturn(Optional.of("Eiffel Tower, Paris, France"));

        GeocodingService service = new GeocodingService(nominatimClient);

        assertThat(service.reverseGeocode(48.8584, 2.2945)).isEqualTo("Eiffel Tower, Paris, France");
    }

    @Test
    void reverseGeocodeThrowsAddressNotFoundWhenNominatimReturnsNothing() {
        when(nominatimClient.reverse(0.0, 0.0))
                .thenReturn(Optional.empty());

        GeocodingService service = new GeocodingService(nominatimClient);

        assertThatThrownBy(() -> service.reverseGeocode(0.0, 0.0))
                .isInstanceOf(AddressNotFoundException.class);
    }
}
