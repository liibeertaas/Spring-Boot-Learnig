package com.example.sales_report_service.service;

import com.example.sales_report_service.model.Region;
import com.example.sales_report_service.model.Sale;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.List;

import static com.example.sales_report_service.service.ReportPeriod.ofMonth;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тести генерації PDF-звіту: перевіряємо сам байтовий вивід (не порожній, валідний PDF),
 * а не текст усередині — підрахунок сум окремо покритий SalesSummaryTest.
 */
class PdfReportServiceTest {

    private final PdfReportService pdfReportService = new PdfReportService(new ChartService());

    @Test
    void generate_shouldReturnNonEmptyByteArray_whenSalesPresent() {
        List<Sale> sales = List.of(
                new Sale("Manager", "Product", new BigDecimal("100.00"), Region.KYIV)
        );

        byte[] pdf = pdfReportService.generate(ofMonth(YearMonth.of(2026, 8)), sales);

        assertThat(pdf).isNotEmpty();
    }

    @Test
    void generate_shouldReturnValidPdfFile_whenSalesPresent() {
        List<Sale> sales = List.of(
                new Sale("Manager", "Product", new BigDecimal("100.00"), Region.KYIV)
        );

        byte[] pdf = pdfReportService.generate(ofMonth(YearMonth.of(2026, 8)), sales);

        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void generate_shouldNotThrow_andReturnValidPdf_whenNoSales() {
        byte[] pdf = pdfReportService.generate(ofMonth(YearMonth.of(2026, 9)), List.of());

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void generate_shouldHandleCyrillicWithoutThrowing() {
        List<Sale> sales = List.of(
                new Sale("Іван Петренко", "Ноутбук", new BigDecimal("12500.50"), Region.KYIV),
                new Sale("Олена Коваль", "Монітор", new BigDecimal("4300.00"), Region.LVIV)
        );

        byte[] pdf = pdfReportService.generate(ofMonth(YearMonth.of(2026, 8)), sales);

        assertThat(pdf).isNotEmpty();
    }

    @Test
    void generate_shouldHandleManySalesAcrossAllRegions() {
        List<Sale> sales = List.of(
                new Sale("A", "P1", new BigDecimal("10.00"), Region.KYIV),
                new Sale("B", "P2", new BigDecimal("20.00"), Region.LVIV),
                new Sale("C", "P3", new BigDecimal("30.00"), Region.ODESA),
                new Sale("D", "P4", new BigDecimal("40.00"), Region.KHARKIV),
                new Sale("E", "P5", new BigDecimal("50.00"), Region.DNIPRO)
        );

        byte[] pdf = pdfReportService.generate(ofMonth(YearMonth.of(2026, 8)), sales);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void generate_shouldHandleArbitraryDateRange_notJustWholeMonth() {
        List<Sale> sales = List.of(
                new Sale("Manager", "Product", new BigDecimal("100.00"), Region.KYIV)
        );
        ReportPeriod arbitraryPeriod = new ReportPeriod(
                java.time.LocalDate.of(2026, 8, 15),
                java.time.LocalDate.of(2026, 9, 10)
        );

        byte[] pdf = pdfReportService.generate(arbitraryPeriod, sales);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }
}
