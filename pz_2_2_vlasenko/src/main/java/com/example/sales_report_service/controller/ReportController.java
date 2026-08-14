package com.example.sales_report_service.controller;

import com.example.sales_report_service.dto.MessageResponseDto;
import com.example.sales_report_service.exception.InvalidReportPeriodException;
import com.example.sales_report_service.model.Sale;
import com.example.sales_report_service.service.ExcelReportService;
import com.example.sales_report_service.service.MailService;
import com.example.sales_report_service.service.PdfReportService;
import com.example.sales_report_service.service.ReportPeriod;
import com.example.sales_report_service.service.SalesService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Період звіту можна задати двома способами (взаємовиключно):
 *  - ?month=2026-09 — цілий календарний місяць;
 *  - ?from=2026-08-15&to=2026-09-15 — довільний діапазон дат.
 */
@RestController
@RequestMapping("/reports")
public class ReportController {

    private static final String XLSX_MEDIA_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final SalesService salesService;
    private final PdfReportService pdfReportService;
    private final ExcelReportService excelReportService;
    private final MailService mailService;

    public ReportController(
            SalesService salesService,
            PdfReportService pdfReportService,
            ExcelReportService excelReportService,
            MailService mailService
    ) {
        this.salesService = salesService;
        this.pdfReportService = pdfReportService;
        this.excelReportService = excelReportService;
        this.mailService = mailService;
    }

    @GetMapping(value = "/sales.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getSalesPdf(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        ReportPeriod period = resolvePeriod(month, from, to);
        List<Sale> sales = salesService.findSales(null, period.from(), period.to());
        byte[] pdf = pdfReportService.generate(period, sales);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=sales-report-" + period.fileSuffix() + ".pdf")
                .body(pdf);
    }

    @GetMapping(value = "/sales.xlsx", produces = XLSX_MEDIA_TYPE)
    public ResponseEntity<byte[]> getSalesExcel(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        ReportPeriod period = resolvePeriod(month, from, to);
        List<Sale> sales = salesService.findSales(null, period.from(), period.to());
        byte[] excel = excelReportService.generate(sales);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=sales-report-" + period.fileSuffix() + ".xlsx")
                .body(excel);
    }

    @PostMapping("/send")
    public ResponseEntity<MessageResponseDto> sendSalesReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        ReportPeriod period = resolvePeriod(month, from, to);
        List<Sale> sales = salesService.findSales(null, period.from(), period.to());
        byte[] pdf = pdfReportService.generate(period, sales);
        List<String> recipients = mailService.sendReport(period, pdf);

        String message = "Звіт надіслано на " + recipients.size() + " отримувачів: " + String.join(", ", recipients);
        return ResponseEntity.ok(new MessageResponseDto(message));
    }

    /**
     * Перетворює параметри запиту на ReportPeriod (валідація — у ReportPeriod.resolve).
     * IllegalArgumentException тут означає некоректну комбінацію month/from/to — перетворюємо
     * на InvalidReportPeriodException, щоб GlobalExceptionHandler віддав акуратний 400.
     */
    private ReportPeriod resolvePeriod(YearMonth month, LocalDate from, LocalDate to) {
        try {
            return ReportPeriod.resolve(month, from, to);
        } catch (IllegalArgumentException e) {
            throw new InvalidReportPeriodException(e.getMessage());
        }
    }
}
