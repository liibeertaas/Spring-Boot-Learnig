package com.example.geo_places_service.dto;

import com.example.geo_places_service.entity.Place;

import java.time.LocalDateTime;

/** Публічне представлення місця, що віддається клієнту. */
public record PlaceResponse(
        Long id,
        String name,
        String address,
        String category,
        Double latitude,
        Double longitude,
        boolean geocoded,
        String geocodingWarning,
        LocalDateTime createdAt
) {
    public static PlaceResponse from(Place place) {
        return from(place, null);
    }

    public static PlaceResponse from(Place place, String geocodingWarning) {
        return new PlaceResponse(
                place.getId(),
                place.getName(),
                place.getAddress(),
                place.getCategory(),
                place.getLatitude(),
                place.getLongitude(),
                place.getLatitude() != null && place.getLongitude() != null,
                geocodingWarning,
                place.getCreatedAt()
        );
    }
}
