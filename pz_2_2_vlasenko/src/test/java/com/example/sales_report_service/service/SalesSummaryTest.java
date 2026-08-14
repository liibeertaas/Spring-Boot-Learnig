package com.example.sales_report_service.service;

import com.example.sales_report_service.model.Region;
import com.example.sales_report_service.model.Sale;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Юніт-тести підрахунку підсумків: загальна сума, розбивка по регіонах, топ-продавці.
 * PDF тут не задіяний — тестується чиста логіка обчислень.
 */
class SalesSummaryTest {

    @Test
    void totalAmount_shouldSumAllSales() {
        List<Sale> sales = List.of(
                new Sale("A", "P1", new BigDecimal("100.00"), Region.KYIV),
                new Sale("B", "P2", new BigDecimal("250.50"), Region.LVIV),
                new Sale("C", "P3", new BigDecimal("49.50"), Region.ODESA)
        );

        SalesSummary summary = SalesSummary.of(sales);

        assertThat(summary.getTotalAmount()).isEqualByComparingTo("400.00");
    }

    @Test
    void totalAmount_shouldBeZero_whenNoSales() {
        SalesSummary summary = SalesSummary.of(List.of());

        assertThat(summary.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void totalsByRegion_shouldGroupAndSumCorrectly() {
        List<Sale> sales = List.of(
                new Sale("A", "P1", new BigDecimal("100.00"), Region.KYIV),
                new Sale("B", "P2", new BigDecimal("50.00"), Region.KYIV),
                new Sale("C", "P3", new BigDecimal("30.00"), Region.LVIV)
        );

        SalesSummary summary = SalesSummary.of(sales);

        assertThat(summary.getTotalsByRegion())
                .hasSize(2)
                .containsEntry(Region.KYIV, new BigDecimal("150.00"))
                .containsEntry(Region.LVIV, new BigDecimal("30.00"));
    }

    @Test
    void totalsByRegion_shouldNotContainRegionsWithoutSales() {
        List<Sale> sales = List.of(
                new Sale("A", "P1", new BigDecimal("100.00"), Region.KYIV)
        );

        SalesSummary summary = SalesSummary.of(sales);

        assertThat(summary.getTotalsByRegion()).containsOnlyKeys(Region.KYIV);
    }

    @Test
    void totalsByRegion_shouldBeEmpty_whenNoSales() {
        SalesSummary summary = SalesSummary.of(List.of());

        assertThat(summary.getTotalsByRegion()).isEmpty();
    }

    @Test
    void totalsByRegion_shouldBeOrderedByEnumDeclarationOrder() {
        // Свідомо додаємо в "неправильному" порядку — DNIPRO, потім KYIV
        List<Sale> sales = List.of(
                new Sale("A", "P1", BigDecimal.TEN, Region.DNIPRO),
                new Sale("B", "P2", BigDecimal.TEN, Region.KYIV)
        );

        SalesSummary summary = SalesSummary.of(sales);

        assertThat(summary.getTotalsByRegion().keySet())
                .containsExactly(Region.KYIV, Region.DNIPRO);
    }

    @Test
    void managersRanked_shouldSortDescendingByTotal() {
        List<Sale> sales = List.of(
                new Sale("Low", "P1", new BigDecimal("10.00"), Region.KYIV),
                new Sale("High", "P2", new BigDecimal("500.00"), Region.LVIV),
                new Sale("Mid", "P3", new BigDecimal("100.00"), Region.ODESA)
        );

        SalesSummary summary = SalesSummary.of(sales);

        assertThat(summary.getManagersRanked())
                .extracting(ManagerTotal::manager)
                .containsExactly("High", "Mid", "Low");
    }

    @Test
    void managersRanked_shouldAggregateMultipleSalesPerManager() {
        List<Sale> sales = List.of(
                new Sale("A", "P1", new BigDecimal("100.00"), Region.KYIV),
                new Sale("A", "P2", new BigDecimal("50.00"), Region.LVIV),
                new Sale("B", "P3", new BigDecimal("30.00"), Region.ODESA)
        );

        SalesSummary summary = SalesSummary.of(sales);

        assertThat(summary.getManagersRanked())
                .extracting(ManagerTotal::manager, ManagerTotal::total)
                .containsExactly(
                        tuple("A", new BigDecimal("150.00")),
                        tuple("B", new BigDecimal("30.00"))
                );
    }

    @Test
    void managersRanked_shouldBeEmpty_whenNoSales() {
        SalesSummary summary = SalesSummary.of(List.of());

        assertThat(summary.getManagersRanked()).isEmpty();
    }
}
