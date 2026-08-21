package com.example.geo_places_service.controller;

import com.example.geo_places_service.dto.ForwardGeocodeResponse;
import com.example.geo_places_service.dto.GeoCoordinates;
import com.example.geo_places_service.dto.ReverseGeocodeResponse;
import com.example.geo_places_service.service.GeocodingService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Геокодування для фронта: адреса <-> координати. */
@RestController
@RequestMapping("/api/geocode")
@RequiredArgsConstructor
@Validated
public class GeocodingController {

    private final GeocodingService geocodingService;

    /**
     * Пряме геокодування (адреса -> координати) для live-підказок у формі:
     * поки користувач вводить вулицю/адресу, фронт дергає цей ендпоінт,
     * щоб одразу підтягнути lat/lon, не чекаючи збереження місця.
     */
    @GetMapping("/forward")
    public ForwardGeocodeResponse forward(@RequestParam @NotBlank String address) {
        GeoCoordinates coordinates = geocodingService.geocode(address);
        return new ForwardGeocodeResponse(address, coordinates.latitude(), coordinates.longitude());
    }

    @GetMapping("/reverse")
    public ReverseGeocodeResponse reverse(
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double lat,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double lon) {
        String address = geocodingService.reverseGeocode(lat, lon);
        return new ReverseGeocodeResponse(lat, lon, address);
    }
}
