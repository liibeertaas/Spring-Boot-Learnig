package com.example.geo_places_service.repository;

import com.example.geo_places_service.entity.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    Page<Place> findByCategoryIgnoreCase(String category, Pageable pageable);

    Page<Place> findByNameContainingIgnoreCase(String namePart, Pageable pageable);

    Page<Place> findByCategoryIgnoreCaseAndNameContainingIgnoreCase(String category, String namePart, Pageable pageable);

    /**
     * Попередня вибірка кандидатів для "пошуку поблизу" через bounding box
     * (прямокутник навколо точки) — щоб НЕ тягнути в пам'ять всю таблицю.
     * Точна відстань (Haversine) рахується вже над цим невеликим набором
     * у сервісному шарі, бо порахувати коло на SQL/JPQL рівні просто й
     * без розширень (PostGIS тощо) неможливо.
     */
    @Query("""
            select p from Place p
            where p.latitude is not null and p.longitude is not null
              and p.latitude between :minLat and :maxLat
              and p.longitude between :minLon and :maxLon
              and (:category is null or lower(p.category) = lower(:category))
            """)
    List<Place> findWithinBoundingBox(
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLon") double minLon,
            @Param("maxLon") double maxLon,
            @Param("category") String category);
}
