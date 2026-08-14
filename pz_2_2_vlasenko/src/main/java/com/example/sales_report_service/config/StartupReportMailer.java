package com.example.sales_report_service.config;

import com.example.sales_report_service.model.Sale;
import com.example.sales_report_service.service.ExcelReportService;
import com.example.sales_report_service.service.MailService;
import com.example.sales_report_service.service.PdfReportService;
import com.example.sales_report_service.service.ReportPeriod;
import com.example.sales_report_service.service.SalesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * При кожному старті застосунку формує PDF + Excel звіт і одразу надсилає на email
 * (report.recipients). Період налаштовується через app.startup-mail.month АБО
 * app.startup-mail.from+to; якщо все порожньо — береться поточний місяць.
 * Можна вимкнути через app.startup-mail.enabled=false (у тестах вимкнено).
 */
@Component
@Order(2) // після DataSeeder — щоб звіт формувався вже з наявними даними
public class StartupReportMailer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupReportMailer.class);

    private final SalesService salesService;
    private final PdfReportService pdfReportService;
    private final ExcelReportService excelReportService;
    private final MailService mailService;
    private final boolean enabled;
    private final String monthConfig;
    private final String fromConfig;
    private final String toConfig;

    public StartupReportMailer(
            SalesService salesService,
            PdfReportService pdfReportService,
            ExcelReportService excelReportService,
            MailService mailService,
            @Value("${app.startup-mail.enabled:true}") boolean enabled,
            @Value("${app.startup-mail.month:}") String monthConfig,
            @Value("${app.startup-mail.from:}") String fromConfig,
            @Value("${app.startup-mail.to:}") String toConfig
    ) {
        this.salesService = salesService;
        this.pdfReportService = pdfReportService;
        this.excelReportService = excelReportService;
        this.mailService = mailService;
        this.enabled = enabled;
        this.monthConfig = monthConfig;
        this.fromConfig = fromConfig;
        this.toConfig = toConfig;
    }

    @Override
    public void run(String... args) {
        if (!enabled) {
            log.info("Автоматична розсилка при старті вимкнена (app.startup-mail.enabled=false)");
            return;
        }

        try {
            ReportPeriod period = resolveConfiguredPeriod();
            log.info("Розсилка звіту при старті за {} — старт", period.label());

            List<Sale> sales = salesService.findSales(null, period.from(), period.to());
            byte[] pdf = pdfReportService.generate(period, sales);
            byte[] excel = excelReportService.generate(sales);
            List<String> recipients = mailService.sendReport(period, pdf, excel);
            log.info("Розсилка звіту при старті за {} — успішно надіслано на {} отримувачів",
                    period.label(), recipients.size());
        } catch (Exception e) {
            // Помилка розсилки (в т.ч. некоректний конфіг періоду) не повинна заважати старту застосунку
            log.error("Розсилка звіту при старті — помилка", e);
        }
    }

    private ReportPeriod resolveConfiguredPeriod() {
        boolean hasMonth = !monthConfig.isBlank();
        boolean hasFrom = !fromConfig.isBlank();
        boolean hasTo = !toConfig.isBlank();

        if (!hasMonth && !hasFrom && !hasTo) {
            return ReportPeriod.ofMonth(YearMonth.now());
        }

        YearMonth month = hasMonth ? YearMonth.parse(monthConfig) : null;
        LocalDate from = hasFrom ? LocalDate.parse(fromConfig) : null;
        LocalDate to = hasTo ? LocalDate.parse(toConfig) : null;

        return ReportPeriod.resolve(month, from, to);
    }
}
