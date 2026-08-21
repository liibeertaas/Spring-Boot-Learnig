package com.example.geo_places_service.exception;

public class PlaceNotFoundException extends RuntimeException {

    public PlaceNotFoundException(Long id) {
        super("Місце з id=%d не знайдено".formatted(id));
    }
}
