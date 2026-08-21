package com.example.geo_places_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @EnableCaching живе в config.CacheConfig разом із CacheManager — щоб @DataJpaTest
// (який не підвантажує наші @Configuration-класи) не намагався активувати кешування
// без зареєстрованого CacheManager.

@SpringBootApplication
public class GeoPlacesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeoPlacesServiceApplication.class, args);
    }

}
