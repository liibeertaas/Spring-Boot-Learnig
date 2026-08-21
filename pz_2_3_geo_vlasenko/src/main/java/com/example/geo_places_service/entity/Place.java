package com.example.geo_places_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Місце користувача ("Мої місця"): назва, адреса, категорія + координати.
 * Координати (latitude/longitude) навмисно nullable:
 *  - на ПЗ-1 їх можна ввести вручну або лишити порожніми;
 *  - на ПЗ-2 порожні координати автоматично підтягуються геокодуванням (Nominatim),
 *    а якщо гео-API недоступний/адресу не знайдено — місце все одно зберігається
 *    (координати лишаються null, їх можна догеокодувати пізніше).
 */
@Entity
@Table(name = "place")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(length = 100)
    private String category;

    private Double latitude;

    private Double longitude;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
