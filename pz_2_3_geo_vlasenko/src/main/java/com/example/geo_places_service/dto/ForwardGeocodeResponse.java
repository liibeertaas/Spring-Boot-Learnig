package com.example.geo_places_service.dto;

/** Відповідь на пряме геокодування (адреса -> координати). Потрібно фронту для live-підказок. */
public record ForwardGeocodeResponse(String address, double latitude, double longitude) {
}
