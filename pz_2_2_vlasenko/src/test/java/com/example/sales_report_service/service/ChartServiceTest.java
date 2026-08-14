package com.example.sales_report_service.service;

import com.example.sales_report_service.model.Region;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тести генерації PNG-діаграми: перевіряємо, що результат — валідний PNG-файл,
 * і що метод не падає на межових випадках (один регіон, усі регіони).
 */
class ChartServiceTest {

    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};

    private final ChartService chartService = new ChartService();

    @Test
    void generateRegionBarChartPng_shouldReturnValidPngSignature() {
        Map<Region, BigDecimal> totals = new EnumMap<>(Region.class);
        totals.put(Region.KYIV, new BigDecimal("100.00"));
        totals.put(Region.LVIV, new BigDecimal("50.00"));

        byte[] png = chartService.generateRegionBarChartPng(totals);

        assertThat(png).isNotEmpty();
        assertThat(java.util.Arrays.copyOfRange(png, 0, 8)).isEqualTo(PNG_SIGNATURE);
    }

    @Test
    void generateRegionBarChartPng_shouldHandleSingleRegion() {
        byte[] png = chartService.generateRegionBarChartPng(Map.of(Region.DNIPRO, BigDecimal.TEN));

        assertThat(png).isNotEmpty();
    }

    @Test
    void generateRegionBarChartPng_shouldHandleAllFiveRegions() {
        Map<Region, BigDecimal> totals = new EnumMap<>(Region.class);
        for (Region region : Region.values()) {
            totals.put(region, BigDecimal.TEN);
        }

        byte[] png = chartService.generateRegionBarChartPng(totals);

        assertThat(png).isNotEmpty();
    }
}
