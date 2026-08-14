package com.example.sales_report_service.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Період звіту (from/to, обидві межі включно). Замінює "лише місяць" — може являти собою
 * рівно один календарний місяць (тоді label() показує "Серпень 2026") або довільний
 * діапазон дат (тоді label() показує "01.08.2026 – 15.09.2026").
 */
public record ReportPeriod(LocalDate from, LocalDate to) {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static ReportPeriod ofMonth(YearMonth month) {
        return new ReportPeriod(month.atDay(1), month.atEndOfMonth());
    }

    /**
     * Приймає рівно один із двох варіантів: тільки month, або тільки пару from+to.
     * Будь-яка інша комбінація (жодного, обидва, лише одне з from/to, from після to) —
     * IllegalArgumentException з описом проблеми.
     */
    public static ReportPeriod resolve(YearMonth month, LocalDate from, LocalDate to) {
        boolean hasMonth = month != null;
        boolean hasFrom = from != null;
        boolean hasTo = to != null;

        if (hasMonth && (hasFrom || hasTo)) {
            throw new IllegalArgumentException(
                    "Вкажіть або 'month', або пару 'from'+'to' — не обидва варіанти одразу");
        }
        if (hasMonth) {
            return ofMonth(month);
        }
        if (hasFrom != hasTo) {
            throw new IllegalArgumentException(
                    "Для довільного періоду потрібні обидва параметри: 'from' і 'to'");
        }
        if (!hasFrom) {
            throw new IllegalArgumentException(
                    "Вкажіть 'month' (напр. 2026-09) або пару 'from'+'to' (напр. from=2026-08-01, to=2026-09-15)");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' не може бути пізніше за 'to'");
        }
        return new ReportPeriod(from, to);
    }

    public String label() {
        YearMonth wholeMonth = wholeMonthOrNull();
        if (wholeMonth != null) {
            return PeriodFormatter.ukrainianLabel(wholeMonth);
        }
        return from.format(DATE_FORMAT) + " – " + to.format(DATE_FORMAT);
    }

    /** Для імені файлу: "2026-08" для цілого місяця, інакше "2026-08-01_2026-09-15". */
    public String fileSuffix() {
        YearMonth wholeMonth = wholeMonthOrNull();
        if (wholeMonth != null) {
            return wholeMonth.toString();
        }
        return from + "_" + to;
    }

    private YearMonth wholeMonthOrNull() {
        YearMonth candidate = YearMonth.from(from);
        boolean isWholeMonth = from.equals(candidate.atDay(1)) && to.equals(candidate.atEndOfMonth());
        return isWholeMonth ? candidate : null;
    }
}
