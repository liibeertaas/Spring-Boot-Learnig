package com.example.geo_places_service.controller;

import com.example.geo_places_service.dto.NearbyPlaceResponse;
import com.example.geo_places_service.dto.PlaceCreateRequest;
import com.example.geo_places_service.dto.PlaceResponse;
import com.example.geo_places_service.dto.PlaceUpdateRequest;
import com.example.geo_places_service.service.PlaceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
@Validated
public class PlaceController {

    private final PlaceService placeService;

    @PostMapping
    public ResponseEntity<PlaceResponse> create(@Valid @RequestBody PlaceCreateRequest request) {
        PlaceResponse created = placeService.create(request);
        return ResponseEntity.created(URI.create("/api/places/" + created.id())).body(created);
    }

    /** Список місць з опційними фільтрами за категорією/частиною назви та пагінацією. */
    @GetMapping
    public Page<PlaceResponse> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name,
            @PageableDefault(size = 20) Pageable pageable) {
        return placeService.list(category, name, pageable);
    }

    /**
     * Місця в радіусі {@code radiusKm} від точки, відсортовані за відстанню (Haversine).
     * Бонус: опційний фільтр за категорією ("кафе в радіусі 3 км").
     */
    @GetMapping("/nearby")
    public Page<NearbyPlaceResponse> nearby(
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double lat,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double lon,
            @RequestParam @Positive double radiusKm,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 20) Pageable pageable) {
        return placeService.findNearby(lat, lon, radiusKm, category, pageable);
    }

    @GetMapping("/{id}")
    public PlaceResponse getById(@PathVariable Long id) {
        return placeService.getById(id);
    }

    @PutMapping("/{id}")
    public PlaceResponse update(@PathVariable Long id, @Valid @RequestBody PlaceUpdateRequest request) {
        return placeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        placeService.delete(id);
    }

    /**
     * Повторна спроба геокодування для місця, збереженого без координат
     * (адресу не було знайдено або Nominatim був недоступний під час створення).
     */
    @PostMapping("/{id}/regeocode")
    public PlaceResponse regeocode(@PathVariable Long id) {
        return placeService.regeocode(id);
    }
}
