package com.example.geo_places_service.exception;

/** Nominatim відповів, але не знайшов жодного результату для заданої адреси/координат. */
public class AddressNotFoundException extends RuntimeException {

    public AddressNotFoundException(String query) {
        super("Не вдалося геокодувати: \"%s\" — адресу/координати не знайдено".formatted(query));
    }
}
