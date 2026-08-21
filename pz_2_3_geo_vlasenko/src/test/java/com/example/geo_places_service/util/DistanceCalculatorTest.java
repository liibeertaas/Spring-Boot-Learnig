package com.example.geo_places_service.util;

import com.example.geo_places_service.service.DistanceCalculator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DistanceCalculatorTest {

    private final DistanceCalculator calculator = new DistanceCalculator();

    @Test
    void sameCoordinatesGiveZeroDistance() {
        double distance = calculator.distanceKm(49.8419, 24.0315, 49.8419, 24.0315);
        assertEquals(0.0, distance, 0.0001);
    }

    @Test
    void kyivToLvivIsApproximatelyKnownDistance() {
        // Київ (Майдан Незалежності) -> Львів (Ринок) ≈ 468 км по прямій (перевірено калькулятором відстаней).
        double kyivLat = 50.4501, kyivLon = 30.5234;
        double lvivLat = 49.8419, lvivLon = 24.0315;

        double distance = calculator.distanceKm(kyivLat, kyivLon, lvivLat, lvivLon);

        assertThat(distance).isCloseTo(468.0, org.assertj.core.data.Offset.offset(10.0));
    }

    @Test
    void distanceIsSymmetric() {
        double a = calculator.distanceKm(49.8419, 24.0315, 50.4501, 30.5234);
        double b = calculator.distanceKm(50.4501, 30.5234, 49.8419, 24.0315);
        assertEquals(a, b, 0.0001);
    }
}
