package com.example.sales_report_service.service;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тести логіки ReportScheduler (не самого cron-тригера — ту частину Spring гарантує сам,
 * а бізнес-логіку методу sendMonthlyReport()).
 */
class ReportSchedulerTest {

    private final SalesService salesService = mock(SalesService.class);
    private final PdfReportService pdfReportService = mock(PdfReportService.class);
    private final MailService mailService = mock(MailService.class);

    private final ReportScheduler scheduler = new ReportScheduler(salesService, pdfReportService, mailService);

    @Test
    void sendMonthlyReport_shouldUsePreviousMonth() {
        ReportPeriod expectedPeriod = ReportPeriod.ofMonth(YearMonth.now().minusMonths(1));
        when(salesService.findSales(isNull(), any(), any())).thenReturn(List.of());
        when(pdfReportService.generate(any(), any())).thenReturn("%PDF-".getBytes());
        when(mailService.sendReport(any(), any())).thenReturn(List.of("boss@example.com"));

        scheduler.sendMonthlyReport();

        verify(salesService).findSales(isNull(), eq(expectedPeriod.from()), eq(expectedPeriod.to()));
        verify(pdfReportService).generate(eq(expectedPeriod), any());
        verify(mailService).sendReport(eq(expectedPeriod), any());
    }

    @Test
    void sendMonthlyReport_shouldNotThrow_whenMailServiceFails() {
        when(salesService.findSales(isNull(), any(), any())).thenReturn(List.of());
        when(pdfReportService.generate(any(), any())).thenReturn("%PDF-".getBytes());
        when(mailService.sendReport(any(), any())).thenThrow(new IllegalStateException("SMTP недоступний"));

        assertThatCode(scheduler::sendMonthlyReport).doesNotThrowAnyException();
    }

    @Test
    void sendMonthlyReport_shouldNotThrow_whenPdfGenerationFails() {
        when(salesService.findSales(isNull(), any(), any())).thenReturn(List.of());
        when(pdfReportService.generate(any(), any())).thenThrow(new RuntimeException("PDF помилка"));

        assertThatCode(scheduler::sendMonthlyReport).doesNotThrowAnyException();
        verify(mailService, never()).sendReport(any(), any());
    }
}
