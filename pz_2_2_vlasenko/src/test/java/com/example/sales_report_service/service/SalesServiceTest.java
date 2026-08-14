package com.example.sales_report_service.service;

import com.example.sales_report_service.dto.SaleRequestDto;
import com.example.sales_report_service.dto.SaleResponseDto;
import com.example.sales_report_service.model.Region;
import com.example.sales_report_service.repository.SalesRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Юніт-тести бізнес-логіки SalesService.
 * SalesRepository використовується реальний (in-memory, без зовнішніх залежностей) — мокати нема сенсу.
 */
class SalesServiceTest {

    private final SalesRepository salesRepository = new SalesRepository();
    private final SalesService salesService = new SalesService(salesRepository);

    @Test
    void addSale_shouldCreateSaleWithGeneratedIdAndCurrentDate() {
        SaleRequestDto request = new SaleRequestDto("Manager", "Product", new BigDecimal("100.00"), Region.KYIV, null);

        SaleResponseDto response = salesService.addSale(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.manager()).isEqualTo("Manager");
        assertThat(response.product()).isEqualTo("Product");
        assertThat(response.amount()).isEqualByComparingTo("100.00");
        assertThat(response.region()).isEqualTo(Region.KYIV);
        assertThat(response.date()).isEqualTo(LocalDate.now());
    }

    @Test
    void getSales_shouldReturnAll_whenNoFiltersGiven() {
        salesService.addSale(new SaleRequestDto("A", "P1", BigDecimal.TEN, Region.KYIV, null));
        salesService.addSale(new SaleRequestDto("B", "P2", BigDecimal.ONE, Region.LVIV, null));

        List<SaleResponseDto> result = salesService.getSales(null, null, null);

        assertThat(result).hasSize(2);
    }

    @Test
    void getSales_shouldFilterByRegion() {
        salesService.addSale(new SaleRequestDto("A", "P1", BigDecimal.TEN, Region.KYIV, null));
        salesService.addSale(new SaleRequestDto("B", "P2", BigDecimal.ONE, Region.LVIV, null));

        List<SaleResponseDto> result = salesService.getSales(Region.KYIV, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).region()).isEqualTo(Region.KYIV);
    }

    @Test
    void addSale_shouldUseExplicitDate_whenProvided() {
        LocalDate pastDate = LocalDate.now().minusMonths(2);
        SaleRequestDto request = new SaleRequestDto("Manager", "Product", new BigDecimal("50.00"), Region.KYIV, pastDate);

        SaleResponseDto response = salesService.addSale(request);

        assertThat(response.date()).isEqualTo(pastDate);
    }

    @Test
    void addSale_shouldDefaultToToday_whenDateNotProvided() {
        SaleRequestDto request = new SaleRequestDto("Manager", "Product", new BigDecimal("50.00"), Region.KYIV, null);

        SaleResponseDto response = salesService.addSale(request);

        assertThat(response.date()).isEqualTo(LocalDate.now());
    }

    @Test
    void getSales_shouldFilterByDateRange() {
        salesService.addSale(new SaleRequestDto("A", "P1", BigDecimal.TEN, Region.KYIV, null));
        LocalDate today = LocalDate.now();

        assertThat(salesService.getSales(null, today, today)).hasSize(1);
        assertThat(salesService.getSales(null, today.plusDays(1), null)).isEmpty();
        assertThat(salesService.getSales(null, null, today.minusDays(1))).isEmpty();
    }
}
