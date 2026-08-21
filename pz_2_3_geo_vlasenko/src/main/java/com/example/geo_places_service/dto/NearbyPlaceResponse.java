package com.example.geo_places_service.dto;

import com.example.geo_places_service.entity.Place;

/** Місце в результатах "поблизу", разом із розрахованою відстанню (Haversine). */
public record NearbyPlaceResponse(
        Long id,
        String name,
        String address,
        String category,
        double latitude,
        double longitude,
        double distanceKm
) {
    public static NearbyPlaceResponse of(Place place, double distanceKm) {
        return new NearbyPlaceResponse(
                place.getId(),
                place.getName(),
                place.getAddress(),
                place.getCategory(),
                place.getLatitude(),
                place.getLongitude(),
                distanceKm
        );
    }
}
