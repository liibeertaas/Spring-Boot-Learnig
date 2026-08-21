package com.example.geo_places_service.controller;

import com.example.geo_places_service.dto.ReverseGeocodeResponse;
import com.example.geo_places_service.service.GeocodingService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Бонус (+3): зворотне геокодування — координати -> адреса. */
@RestController
@RequestMapping("/api/geocode")
@RequiredArgsConstructor
@Validated
public class GeocodingController {

    private final GeocodingService geocodingService;

    @GetMapping("/reverse")
    public ReverseGeocodeResponse reverse(
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double lat,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double lon) {
        String address = geocodingService.reverseGeocode(lat, lon);
        return new ReverseGeocodeResponse(lat, lon, address);
    }
}
