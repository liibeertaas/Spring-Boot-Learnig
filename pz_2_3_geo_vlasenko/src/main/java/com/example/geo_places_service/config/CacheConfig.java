package com.example.geo_places_service.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Кеш результатів геокодування: та сама адреса (або та сама пара координат
 * для reverse-геокодування) не повинна йти в Nominatim двічі.
 * In-memory кеш (ConcurrentMapCacheManager) достатній для навчального проєкту —
 * живе, поки живий процес застосунку; для persist-кешу між рестартами
 * можна замінити на БД-таблицю аналогічно {@code Place}.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String GEOCODE_CACHE = "geocodeCache";
    public static final String REVERSE_GEOCODE_CACHE = "reverseGeocodeCache";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(GEOCODE_CACHE, REVERSE_GEOCODE_CACHE);
    }
}
