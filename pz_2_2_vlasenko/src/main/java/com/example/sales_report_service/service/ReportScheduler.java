package com.example.sales_report_service.service;

import com.example.sales_report_service.model.Sale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;

/**
 * Автоматична щомісячна розсилка звіту про продажі за попередній місяць.
 * Запускається 1-го числа кожного місяця о 03:00 (Europe/Kyiv).
 */
@Component
public class ReportScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReportScheduler.class);

    private final SalesService salesService;
    private final PdfReportService pdfReportService;
    private final MailService mailService;

    public ReportScheduler(SalesService salesService, PdfReportService pdfReportService, MailService mailService) {
        this.salesService = salesService;
        this.pdfReportService = pdfReportService;
        this.mailService = mailService;
    }

    @Scheduled(cron = "0 0 3 1 * *", zone = "Europe/Kyiv")
    public void sendMonthlyReport() {
        ReportPeriod period = ReportPeriod.ofMonth(YearMonth.now().minusMonths(1));
        log.info("Автоматична розсилка звіту за {} — старт", period.label());

        try {
            List<Sale> sales = salesService.findSales(null, period.from(), period.to());
            byte[] pdf = pdfReportService.generate(period, sales);
            List<String> recipients = mailService.sendReport(period, pdf);
            log.info("Автоматична розсилка звіту за {} — успішно надіслано на {} отримувачів",
                    period.label(), recipients.size());
        } catch (Exception e) {
            // Помилка розсилки не повинна "покласти" застосунок чи наступні заплановані запуски
            log.error("Автоматична розсилка звіту за {} — помилка", period.label(), e);
        }
    }
}
