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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private static final Logger log = LoggerFactory.getLogger(PlaceService.class);

    private final PlaceRepository placeRepository;
    private final GeocodingService geocodingService;
    private final DistanceCalculator distanceCalculator;

    @Transactional
    public PlaceResponse create(PlaceCreateRequest request) {
        Place place = Place.builder()
                .name(request.name())
                .address(request.address())
                .category(request.category())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build();

        String geocodingWarning = null;
        if (place.getLatitude() == null || place.getLongitude() == null) {
            geocodingWarning = tryGeocode(place);
        }

        Place saved = placeRepository.save(place);
        return PlaceResponse.from(saved, geocodingWarning);
    }

    /**
     * Геокодує адресу місця. Якщо Nominatim не знайшов адресу або недоступний —
     * місце все одно зберігається (координати лишаються null), а причина повертається
     * як попередження, а НЕ як 500/виняток, що зупиняє створення.
     */
    private String tryGeocode(Place place) {
        try {
            GeoCoordinates coordinates = geocodingService.geocode(place.getAddress());
            place.setLatitude(coordinates.latitude());
            place.setLongitude(coordinates.longitude());
            return null;
        } catch (AddressNotFoundException ex) {
            log.warn("Геокодування: адресу не знайдено — {}", ex.getMessage());
            return "Адресу не вдалося геокодувати автоматично (Nominatim нічого не знайшов). "
                    + "Місце збережено без координат, їх можна додати вручну або догеокодувати пізніше.";
        } catch (GeocodingUnavailableException ex) {
            log.warn("Геокодування: гео-сервіс недоступний — {}", ex.getMessage());
            return "Гео-сервіс тимчасово недоступний. Місце збережено без координат, "
                    + "спробуйте догеокодувати пізніше (POST /api/places/{id}/regeocode).";
        }
    }

    @Transactional
    public PlaceResponse regeocode(Long id) {
        Place place = findOrThrow(id);
        String warning = tryGeocode(place);
        return PlaceResponse.from(place, warning);
    }

    /** Список місць із опційними фільтрами за категорією/частиною назви + пагінацією. */
    public Page<PlaceResponse> list(String category, String name, Pageable pageable) {
        boolean hasCategory = StringUtils.hasText(category);
        boolean hasName = StringUtils.hasText(name);

        Page<Place> page;
        if (hasCategory && hasName) {
            page = placeRepository.findByCategoryIgnoreCaseAndNameContainingIgnoreCase(category, name, pageable);
        } else if (hasCategory) {
            page = placeRepository.findByCategoryIgnoreCase(category, pageable);
        } else if (hasName) {
            page = placeRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            page = placeRepository.findAll(pageable);
        }
        return page.map(PlaceResponse::from);
    }

    /**
     * Пошук місць у радіусі {@code radiusKm} від точки (lat, lon), відсортованих за відстанню
     * (формула Haversine, {@link DistanceCalculator}). Кандидатів попередньо звужуємо
     * bounding-box запитом у БД, щоб не тягнути всю таблицю в пам'ять.
     */
    public Page<NearbyPlaceResponse> findNearby(double lat, double lon, double radiusKm, String category, Pageable pageable) {
        // 1 градус широти ≈ 111.32 км; довгота стискається за косинусом широти.
        double latDeltaDeg = radiusKm / 111.32;
        double lonDeltaDeg = radiusKm / (111.32 * Math.max(Math.cos(Math.toRadians(lat)), 0.000001));

        List<Place> candidates = placeRepository.findWithinBoundingBox(
                lat - latDeltaDeg, lat + latDeltaDeg,
                lon - lonDeltaDeg, lon + lonDeltaDeg,
                StringUtils.hasText(category) ? category : null);

        List<NearbyPlaceResponse> withinRadius = candidates.stream()
                .map(p -> NearbyPlaceResponse.of(p, distanceCalculator.distanceKm(lat, lon, p.getLatitude(), p.getLongitude())))
                .filter(r -> r.distanceKm() <= radiusKm)
                .sorted(Comparator.comparingDouble(NearbyPlaceResponse::distanceKm))
                .toList();

        int start = (int) pageable.getOffset();
        if (start >= withinRadius.size()) {
            return new PageImpl<>(List.of(), pageable, withinRadius.size());
        }
        int end = Math.min(start + pageable.getPageSize(), withinRadius.size());
        return new PageImpl<>(withinRadius.subList(start, end), pageable, withinRadius.size());
    }

    public PlaceResponse getById(Long id) {
        return PlaceResponse.from(findOrThrow(id));
    }

    @Transactional
    public PlaceResponse update(Long id, PlaceUpdateRequest request) {
        Place place = findOrThrow(id);
        place.setName(request.name());
        place.setAddress(request.address());
        place.setCategory(request.category());
        place.setLatitude(request.latitude());
        place.setLongitude(request.longitude());
        return PlaceResponse.from(place);
    }

    @Transactional
    public void delete(Long id) {
        if (!placeRepository.existsById(id)) {
            throw new PlaceNotFoundException(id);
        }
        placeRepository.deleteById(id);
    }

    private Place findOrThrow(Long id) {
        return placeRepository.findById(id)
                .orElseThrow(() -> new PlaceNotFoundException(id));
    }
}
