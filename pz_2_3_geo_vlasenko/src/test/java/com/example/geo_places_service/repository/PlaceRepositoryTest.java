package com.example.geo_places_service.repository;

import com.example.geo_places_service.entity.Place;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PlaceRepositoryTest {

    @org.springframework.beans.factory.annotation.Autowired
    private PlaceRepository placeRepository;

    private Place place(String name, String category, Double lat, Double lon) {
        return placeRepository.save(Place.builder()
                .name(name)
                .address(name + " address")
                .category(category)
                .latitude(lat)
                .longitude(lon)
                .build());
    }

    @Test
    void savesAndFindsPlaceById() {
        Place saved = place("Cafe Central", "cafe", 49.84, 24.03);

        assertThat(placeRepository.findById(saved.getId())).isPresent();
        assertThat(placeRepository.findById(saved.getId()).get().getName()).isEqualTo("Cafe Central");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void findByCategoryIgnoreCaseIsCaseInsensitiveAndPaged() {
        place("Cafe A", "cafe", 49.0, 24.0);
        place("Cafe B", "cafe", 49.1, 24.1);
        place("Museum", "museum", 50.0, 30.0);

        Page<Place> page = placeRepository.findByCategoryIgnoreCase("CAFE", PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(Place::getCategory).containsOnly("cafe");
    }

    @Test
    void findByNameContainingIgnoreCaseMatchesPartial() {
        place("Lviv Coffee House", "cafe", 49.0, 24.0);
        place("Kyiv Museum", "museum", 50.0, 30.0);

        Page<Place> page = placeRepository.findByNameContainingIgnoreCase("coffee", PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Lviv Coffee House");
    }

    @Test
    void findWithinBoundingBoxExcludesFarAndNullCoordinatePlaces() {
        place("Near", "cafe", 49.85, 24.04);
        place("Far", "museum", 50.45, 30.52); // Kyiv, далеко від bounding box навколо Львова
        place("NoCoords", "cafe", null, null);

        List<Place> nearby = placeRepository.findWithinBoundingBox(49.5, 50.0, 23.5, 24.5, null);

        assertThat(nearby).extracting(Place::getName).containsExactly("Near");
    }

    @Test
    void findWithinBoundingBoxAppliesCategoryFilter() {
        place("Near Cafe", "cafe", 49.85, 24.04);
        place("Near Museum", "museum", 49.86, 24.05);

        List<Place> cafesOnly = placeRepository.findWithinBoundingBox(49.5, 50.0, 23.5, 24.5, "cafe");

        assertThat(cafesOnly).extracting(Place::getName).containsExactly("Near Cafe");
    }
}
