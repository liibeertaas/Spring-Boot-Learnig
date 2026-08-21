package com.example.geo_places_service.service;

import com.example.geo_places_service.dto.GeoCoordinates;
import com.example.geo_places_service.dto.NearbyPlaceResponse;
import com.example.geo_places_service.dto.PlaceCreateRequest;
import com.example.geo_places_service.dto.PlaceResponse;
import com.example.geo_places_service.dto.PlaceUpdateRequest;
import com.example.geo_places_service.entity.Place;
import com.example.geo_places_service.exception.AddressNotFoundException;
import com.example.geo_places_service.exception.GeocodingUnavailableException;
import com.example.geo_places_service.exception.PlaceNotFoundException;
import com.example.geo_places_service.repository.PlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юніт-тести бізнес-логіки {@link PlaceService} з мок-репозиторієм і мок-геосервісом
 * (мережа/БД не задіяні). {@link DistanceCalculator} — справжній, бо це чиста функція.
 */
@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private GeocodingService geocodingService;

    private PlaceService placeService;

    @BeforeEach
    void setUp() {
        placeService = new PlaceService(placeRepository, geocodingService, new DistanceCalculator());
    }

    private Place place(Long id, String name, String category, Double lat, Double lon) {
        return Place.builder().id(id).name(name).address(name + " address").category(category)
                .latitude(lat).longitude(lon).build();
    }

    // ---------- create ----------

    @Test
    void createWithManualCoordinatesDoesNotCallGeocoding() {
        PlaceCreateRequest request = new PlaceCreateRequest("Cafe", "Rynok 1, Lviv", "cafe", 49.84, 24.03);
        when(placeRepository.save(any(Place.class))).thenAnswer(inv -> inv.getArgument(0));

        PlaceResponse response = placeService.create(request);

        verify(geocodingService, never()).geocode(anyString());
        assertThat(response.latitude()).isEqualTo(49.84);
        assertThat(response.geocoded()).isTrue();
        assertThat(response.geocodingWarning()).isNull();
    }

    @Test
    void createWithoutCoordinatesGeocodesAddress() {
        PlaceCreateRequest request = new PlaceCreateRequest("Cafe", "Rynok 1, Lviv", "cafe", null, null);
        when(geocodingService.geocode("Rynok 1, Lviv")).thenReturn(new GeoCoordinates(49.8419, 24.0315));
        when(placeRepository.save(any(Place.class))).thenAnswer(inv -> inv.getArgument(0));

        PlaceResponse response = placeService.create(request);

        assertThat(response.latitude()).isEqualTo(49.8419);
        assertThat(response.longitude()).isEqualTo(24.0315);
        assertThat(response.geocodingWarning()).isNull();
    }

    @Test
    void createSavesPlaceWithoutCoordinatesWhenAddressNotFound_insteadOfFailing() {
        PlaceCreateRequest request = new PlaceCreateRequest("Ghost", "nowhere 999", "test", null, null);
        when(geocodingService.geocode("nowhere 999")).thenThrow(new AddressNotFoundException("nowhere 999"));
        when(placeRepository.save(any(Place.class))).thenAnswer(inv -> inv.getArgument(0));

        PlaceResponse response = placeService.create(request);

        assertThat(response.latitude()).isNull();
        assertThat(response.longitude()).isNull();
        assertThat(response.geocoded()).isFalse();
        assertThat(response.geocodingWarning()).isNotBlank();
        verify(placeRepository, times(1)).save(any(Place.class));
    }

    @Test
    void createSavesPlaceWithoutCoordinatesWhenGeocodingServiceUnavailable_insteadOfFailing() {
        PlaceCreateRequest request = new PlaceCreateRequest("Cafe", "Rynok 1, Lviv", "cafe", null, null);
        when(geocodingService.geocode(anyString()))
                .thenThrow(new GeocodingUnavailableException("timeout", new RuntimeException()));
        when(placeRepository.save(any(Place.class))).thenAnswer(inv -> inv.getArgument(0));

        PlaceResponse response = placeService.create(request);

        assertThat(response.geocoded()).isFalse();
        assertThat(response.geocodingWarning()).contains("недоступний");
    }

    // ---------- getById / update / delete ----------

    @Test
    void getByIdReturnsPlaceWhenFound() {
        when(placeRepository.findById(1L)).thenReturn(Optional.of(place(1L, "Cafe", "cafe", 49.0, 24.0)));

        PlaceResponse response = placeService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Cafe");
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        when(placeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placeService.getById(999L))
                .isInstanceOf(PlaceNotFoundException.class);
    }

    @Test
    void updateChangesAllFields() {
        Place existing = place(1L, "Old name", "old", 1.0, 1.0);
        when(placeRepository.findById(1L)).thenReturn(Optional.of(existing));
        PlaceUpdateRequest request = new PlaceUpdateRequest("New name", "New address", "new", 2.0, 2.0);

        PlaceResponse response = placeService.update(1L, request);

        assertThat(response.name()).isEqualTo("New name");
        assertThat(response.category()).isEqualTo("new");
        assertThat(response.latitude()).isEqualTo(2.0);
    }

    @Test
    void deleteRemovesExistingPlace() {
        when(placeRepository.existsById(1L)).thenReturn(true);

        placeService.delete(1L);

        verify(placeRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteThrowsWhenPlaceDoesNotExist() {
        when(placeRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> placeService.delete(999L))
                .isInstanceOf(PlaceNotFoundException.class);
        verify(placeRepository, never()).deleteById(any());
    }

    // ---------- list (фільтри) ----------

    @Test
    void listWithoutFiltersCallsFindAll() {
        Pageable pageable = PageRequest.of(0, 20);
        when(placeRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

        placeService.list(null, null, pageable);

        verify(placeRepository).findAll(pageable);
        verify(placeRepository, never()).findByCategoryIgnoreCase(anyString(), eq(pageable));
    }

    @Test
    void listWithCategoryOnlyCallsCategoryFilter() {
        Pageable pageable = PageRequest.of(0, 20);
        when(placeRepository.findByCategoryIgnoreCase("cafe", pageable)).thenReturn(new PageImpl<>(List.of()));

        placeService.list("cafe", null, pageable);

        verify(placeRepository).findByCategoryIgnoreCase("cafe", pageable);
    }

    @Test
    void listWithCategoryAndNameCallsCombinedFilter() {
        Pageable pageable = PageRequest.of(0, 20);
        when(placeRepository.findByCategoryIgnoreCaseAndNameContainingIgnoreCase("cafe", "central", pageable))
                .thenReturn(new PageImpl<>(List.of()));

        placeService.list("cafe", "central", pageable);

        verify(placeRepository).findByCategoryIgnoreCaseAndNameContainingIgnoreCase("cafe", "central", pageable);
    }

    // ---------- findNearby (Haversine + сортування + пагінація) ----------

    @Test
    void findNearbySortsByDistanceAscendingAndFiltersOutOfRadius() {
        // Точка відліку: Ринок площа, Львів
        double lat = 49.8419, lon = 24.0315;
        Place near = place(1L, "Near", "cafe", 49.8430, 24.0320);   // ~0.13 км
        Place mid = place(2L, "Mid", "cafe", 49.90, 24.05);          // ~6.6 км
        Place far = place(3L, "Far (Kyiv)", "museum", 50.4501, 30.5234); // ~467 км, поза радіусом 10км

        // Bounding-box для 10км навколо lat/lon мав би виключити Київ ще на рівні repository,
        // але тут ми контролюємо саме те, що повертає репозиторій, і перевіряємо фільтр у сервісі.
        when(placeRepository.findWithinBoundingBox(any(Double.class), any(Double.class), any(Double.class), any(Double.class), eq((String) null)))
                .thenReturn(List.of(mid, near, far));

        Page<NearbyPlaceResponse> result = placeService.findNearby(lat, lon, 10.0, null, PageRequest.of(0, 20));

        assertThat(result.getContent()).extracting(NearbyPlaceResponse::name).containsExactly("Near", "Mid");
        assertThat(result.getContent()).extracting(NearbyPlaceResponse::id).doesNotContain(3L);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).distanceKm()).isLessThan(result.getContent().get(1).distanceKm());
    }

    @Test
    void findNearbyPassesCategoryFilterToRepository() {
        ArgumentCaptor<String> categoryCaptor = ArgumentCaptor.forClass(String.class);
        when(placeRepository.findWithinBoundingBox(any(Double.class), any(Double.class), any(Double.class), any(Double.class), categoryCaptor.capture()))
                .thenReturn(List.of());

        placeService.findNearby(49.84, 24.03, 5.0, "cafe", PageRequest.of(0, 20));

        assertThat(categoryCaptor.getValue()).isEqualTo("cafe");
    }

    @Test
    void findNearbyPaginatesManuallyOverSortedResults() {
        double lat = 0.0, lon = 0.0;
        // 5 місць на однаковій "довготі", різні широти -> різні (передбачувані) відстані
        List<Place> places = List.of(
                place(1L, "P1", "x", 0.01, 0.0),
                place(2L, "P2", "x", 0.02, 0.0),
                place(3L, "P3", "x", 0.03, 0.0),
                place(4L, "P4", "x", 0.04, 0.0),
                place(5L, "P5", "x", 0.05, 0.0)
        );
        when(placeRepository.findWithinBoundingBox(any(Double.class), any(Double.class), any(Double.class), any(Double.class), eq((String) null)))
                .thenReturn(places);

        Page<NearbyPlaceResponse> firstPage = placeService.findNearby(lat, lon, 1000.0, null, PageRequest.of(0, 2));
        Page<NearbyPlaceResponse> secondPage = placeService.findNearby(lat, lon, 1000.0, null, PageRequest.of(1, 2));

        assertThat(firstPage.getContent()).extracting(NearbyPlaceResponse::name).containsExactly("P1", "P2");
        assertThat(secondPage.getContent()).extracting(NearbyPlaceResponse::name).containsExactly("P3", "P4");
        assertThat(firstPage.getTotalElements()).isEqualTo(5);
        assertThat(firstPage.getTotalPages()).isEqualTo(3);
    }

    // ---------- regeocode ----------

    @Test
    void regeocodeUpdatesCoordinatesWhenAddressNowFound() {
        Place existing = place(1L, "Cafe", "cafe", null, null);
        when(placeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(geocodingService.geocode(existing.getAddress())).thenReturn(new GeoCoordinates(49.84, 24.03));

        PlaceResponse response = placeService.regeocode(1L);

        assertThat(response.latitude()).isEqualTo(49.84);
        assertThat(response.geocodingWarning()).isNull();
    }

    @Test
    void regeocodeThrowsWhenPlaceNotFound() {
        when(placeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placeService.regeocode(999L))
                .isInstanceOf(PlaceNotFoundException.class);
    }
}
