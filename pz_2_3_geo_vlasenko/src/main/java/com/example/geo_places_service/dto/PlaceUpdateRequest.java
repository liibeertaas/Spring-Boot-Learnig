package com.example.geo_places_service.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Запит на повне оновлення місця (PUT /places/{id}). */
public record PlaceUpdateRequest(

        @NotBlank(message = "Назва не може бути порожньою")
        @Size(max = 255, message = "Назва занадто довга (максимум 255 символів)")
        String name,

        @NotBlank(message = "Адреса не може бути порожньою")
        @Size(max = 500, message = "Адреса занадто довга (максимум 500 символів)")
        String address,

        @Size(max = 100, message = "Категорія занадто довга (максимум 100 символів)")
        String category,

        @DecimalMin(value = "-90.0", message = "Широта має бути в діапазоні [-90; 90]")
        @DecimalMax(value = "90.0", message = "Широта має бути в діапазоні [-90; 90]")
        Double latitude,

        @DecimalMin(value = "-180.0", message = "Довгота має бути в діапазоні [-180; 180]")
        @DecimalMax(value = "180.0", message = "Довгота має бути в діапазоні [-180; 180]")
        Double longitude
) {
}
