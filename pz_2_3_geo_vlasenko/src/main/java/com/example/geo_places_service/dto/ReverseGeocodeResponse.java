package com.example.geo_places_service.dto;

/** Відповідь на зворотне геокодування (координати -> адреса). Бонус +3. */
public record ReverseGeocodeResponse(double latitude, double longitude, String address) {
}
