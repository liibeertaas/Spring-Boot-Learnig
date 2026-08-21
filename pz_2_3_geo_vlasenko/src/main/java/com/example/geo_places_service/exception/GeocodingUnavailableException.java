package com.example.geo_places_service.exception;

/** Nominatim недоступний / тайм-аут / 5xx — тимчасова проблема, не помилка клієнта. */
public class GeocodingUnavailableException extends RuntimeException {

    public GeocodingUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
