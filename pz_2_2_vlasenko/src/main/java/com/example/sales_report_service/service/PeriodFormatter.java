package com.example.sales_report_service.service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Форматування місяця українською (називний відмінок, з великої літери): "Серпень 2026".
 * Спільна утиліта для PdfReportService і MailService — щоб не дублювати форматування.
 */
public final class PeriodFormatter {

    private static final DateTimeFormatter UKRAINIAN_MONTH_YEAR =
            DateTimeFormatter.ofPattern("LLLL yyyy", new Locale("uk"));

    private PeriodFormatter() {
    }

    public static String ukrainianLabel(YearMonth month) {
        String raw = month.format(UKRAINIAN_MONTH_YEAR);
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }
}
