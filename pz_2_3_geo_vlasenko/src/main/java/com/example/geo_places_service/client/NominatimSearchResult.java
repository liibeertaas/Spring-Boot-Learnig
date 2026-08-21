package com.example.geo_places_service.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Відповідь Nominatim /search — лише поля, які нам потрібні
 * (lat/lon у Nominatim приходять як рядки).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NominatimSearchResult(String lat, String lon, String display_name) {
}
