package com.example.sales_report_service.controller;

import com.example.sales_report_service.model.Region;
import com.example.sales_report_service.model.Sale;
import com.example.sales_report_service.service.ExcelReportService;
import com.example.sales_report_service.service.MailService;
import com.example.sales_report_service.service.PdfReportService;
import com.example.sales_report_service.service.ReportPeriod;
import com.example.sales_report_service.service.SalesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тести HTTP-шару ReportController: статуси відповідей, коректність параметра month,
 * заголовки відповіді. SalesService і PdfReportService замінені мок-бинами.
 */
@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SalesService salesService;

    @MockitoBean
    private PdfReportService pdfReportService;

    @MockitoBean
    private ExcelReportService excelReportService;

    @MockitoBean
    private MailService mailService;

    @Test
    void getSalesPdf_shouldReturn200AndPdfContentType_whenMonthValid() throws Exception {
        byte[] fakePdf = "%PDF-fake".getBytes();
        when(salesService.findSales(isNull(), any(), any())).thenReturn(List.of());
        when(pdfReportService.generate(any(), any())).thenReturn(fakePdf);

        mockMvc.perform(get("/reports/sales.pdf").param("month", "2026-08"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE))
                .andExpect(content().bytes(fakePdf));
    }

    @Test
    void getSalesPdf_shouldFilterByFirstAndLastDayOfMonth() throws Exception {
        when(salesService.findSales(isNull(), any(), any())).thenReturn(List.of());
        when(pdfReportService.generate(any(), any())).thenReturn("%PDF-".getBytes());

        mockMvc.perform(get("/reports/sales.pdf").param("month", "2026-02"))
                .andExpect(status().isOk());

        verify(salesService).findSales(isNull(), eq(LocalDate.of(2026, 2, 1)), eq(LocalDate.of(2026, 2, 28)));
    }

    @Test
    void getSalesPdf_shouldReturn400_whenMonthMissing() throws Exception {
        mockMvc.perform(get("/reports/sales.pdf"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"status\":400}", false));
    }

    @Test
    void getSalesPdf_shouldReturn400_whenMonthFormatInvalid() throws Exception {
        mockMvc.perform(get("/reports/sales.pdf").param("month", "not-a-month"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSalesPdf_shouldPassSalesFromServiceToPdfGenerator() throws Exception {
        List<Sale> sales = List.of(new Sale("Manager", "Product", new BigDecimal("10.00"), Region.KYIV));
        when(salesService.findSales(isNull(), any(), any())).thenReturn(sales);
        when(pdfReportService.generate(any(), eq(sales))).thenReturn("%PDF-".getBytes());

        mockMvc.perform(get("/reports/sales.pdf").param("month", "2026-08"))
                .andExpect(status().isOk());
    }

    @Test
    void getSalesExcel_shouldReturn200AndXlsxContentType_whenMonthValid() throws Exception {
        byte[] fakeExcel = "fake-xlsx-content".getBytes();
        when(salesService.findSales(isNull(), any(), any())).thenReturn(List.of());
        when(excelReportService.generate(any())).thenReturn(fakeExcel);

        mockMvc.perform(get("/reports/sales.xlsx").param("month", "2026-08"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(content().bytes(fakeExcel));
    }

    @Test
    void getSalesExcel_shouldReturn400_whenMonthMissing() throws Exception {
        mockMvc.perform(get("/reports/sales.xlsx"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSalesExcel_shouldFilterByFirstAndLastDayOfMonth() throws Exception {
        when(salesService.findSales(isNull(), any(), any())).thenReturn(List.of());
        when(excelReportService.generate(any())).thenReturn("xlsx".getBytes());

        mockMvc.perform(get("/reports/sales.xlsx").param("month", "2026-02"))
                .andExpect(status().isOk());

        verify(salesService).findSales(isNull(), eq(LocalDate.of(2026, 2, 1)), eq(LocalDate.of(2026, 2, 28)));
    }

    @Test
    void sendSalesReport_shouldReturn200AndConfirmationMessage() throws Exception {
        when(salesService.findSales(isNull(), any(), any())).thenReturn(List.of());
        when(pdfReportService.generate(any(), any())).thenReturn("%PDF-".getBytes());
        when(mailService.sendReport(any(), any())).thenReturn(List.of("boss1@example.com", "boss2@example.com"));

        mockMvc.perform(post("/reports/send").param("month", "2026-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "Звіт надіслано на 2 отримувачів: boss1@example.com, boss2@example.com"));
    }

    @Test
    void sendSalesReport_shouldCallMailServiceWithGeneratedPdf() throws Exception {
        byte[] fakePdf = "%PDF-fake".getBytes();
        when(salesService.findSales(isNull(), any(), any())).thenReturn(List.of());
        when(pdfReportService.generate(any(), any())).thenReturn(fakePdf);
        when(mailService.sendReport(any(), any())).thenReturn(List.of("boss1@example.com"));

        mockMvc.perform(post("/reports/send").param("month", "2026-08"))
                .andExpect(status().isOk());

        verify(mailService).sendReport(eq(ReportPeriod.ofMonth(YearMonth.of(2026, 8))), eq(fakePdf));
    }

    @Test
    void sendSalesReport_shouldReturn400_whenMonthMissing() throws Exception {
        mockMvc.perform(post("/reports/send"))
                .andExpect(status().isBadRequest());

        verify(mailService, never()).sendReport(any(), any());
    }

    @Test
    void sendSalesReport_shouldReturn500_whenMailServiceFails() throws Exception {
        when(salesService.findSales(isNull(), any(), any())).thenReturn(List.of());
        when(pdfReportService.generate(any(), any())).thenReturn("%PDF-".getBytes());
        when(mailService.sendReport(any(), any())).thenThrow(new IllegalStateException("SMTP down"));

        mockMvc.perform(post("/reports/send").param("month", "2026-08"))
                .andExpect(status().isInternalServerError());
    }

    // --- Довільний період (from/to) — бонус: період не лише "цілий місяць" ---

    @Test
    void getSalesPdf_shouldReturn200_whenArbitraryFromToGiven() throws Exception {
        when(salesService.findSales(isNull(), any(), any())).thenReturn(List.of());
        when(pdfReportService.generate(any(), any())).thenReturn("%PDF-".getBytes());

        mockMvc.perform(get("/reports/sales.pdf")
                        .param("from", "2026-08-15")
                        .param("to", "2026-09-10"))
                .andExpect(status().isOk());

        verify(salesService).findSales(isNull(), eq(LocalDate.of(2026, 8, 15)), eq(LocalDate.of(2026, 9, 10)));
        verify(pdfReportService).generate(
                eq(new ReportPeriod(LocalDate.of(2026, 8, 15), LocalDate.of(2026, 9, 10))), any());
    }

    @Test
    void getSalesPdf_shouldReturn400_whenBothMonthAndFromToGiven() throws Exception {
        mockMvc.perform(get("/reports/sales.pdf")
                        .param("month", "2026-08")
                        .param("from", "2026-08-01")
                        .param("to", "2026-08-31"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSalesPdf_shouldReturn400_whenOnlyFromGivenWithoutTo() throws Exception {
        mockMvc.perform(get("/reports/sales.pdf").param("from", "2026-08-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSalesPdf_shouldReturn400_whenOnlyToGivenWithoutFrom() throws Exception {
        mockMvc.perform(get("/reports/sales.pdf").param("to", "2026-08-31"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSalesPdf_shouldReturn400_whenFromAfterTo() throws Exception {
        mockMvc.perform(get("/reports/sales.pdf")
                        .param("from", "2026-09-01")
                        .param("to", "2026-08-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSalesExcel_shouldReturn200_whenArbitraryFromToGiven() throws Exception {
        when(salesService.findSales(isNull(), any(), any())).thenReturn(List.of());
        when(excelReportService.generate(any())).thenReturn("xlsx".getBytes());

        mockMvc.perform(get("/reports/sales.xlsx")
                        .param("from", "2026-08-15")
                        .param("to", "2026-09-10"))
                .andExpect(status().isOk());

        verify(salesService).findSales(isNull(), eq(LocalDate.of(2026, 8, 15)), eq(LocalDate.of(2026, 9, 10)));
    }

    @Test
    void sendSalesReport_shouldReturn200_whenArbitraryFromToGiven() throws Exception {
        when(salesService.findSales(isNull(), any(), any())).thenReturn(List.of());
        when(pdfReportService.generate(any(), any())).thenReturn("%PDF-".getBytes());
        when(mailService.sendReport(any(), any())).thenReturn(List.of("boss1@example.com"));

        mockMvc.perform(post("/reports/send")
                        .param("from", "2026-08-15")
                        .param("to", "2026-09-10"))
                .andExpect(status().isOk());

        verify(mailService).sendReport(
                eq(new ReportPeriod(LocalDate.of(2026, 8, 15), LocalDate.of(2026, 9, 10))), any());
    }
}
