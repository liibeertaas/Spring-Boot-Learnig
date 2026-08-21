package com.example.geo_places_service.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Відповідь Nominatim /reverse. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NominatimReverseResult(String display_name) {
}
