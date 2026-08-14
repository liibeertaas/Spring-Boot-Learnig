package com.example.sales_report_service.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportPeriodTest {

    @Test
    void ofMonth_shouldSetFromAndToToMonthBoundaries() {
        ReportPeriod period = ReportPeriod.ofMonth(YearMonth.of(2026, 2));

        assertThat(period.from()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(period.to()).isEqualTo(LocalDate.of(2026, 2, 28));
    }

    @Test
    void label_shouldShowUkrainianMonthName_whenPeriodIsWholeMonth() {
        ReportPeriod period = ReportPeriod.ofMonth(YearMonth.of(2026, 8));

        assertThat(period.label()).isEqualTo("Серпень 2026");
    }

    @Test
    void label_shouldShowDateRange_whenPeriodIsNotWholeMonth() {
        ReportPeriod period = new ReportPeriod(LocalDate.of(2026, 8, 15), LocalDate.of(2026, 9, 10));

        assertThat(period.label()).isEqualTo("15.08.2026 – 10.09.2026");
    }

    @Test
    void label_shouldShowDateRange_whenSingleDayPeriod() {
        ReportPeriod period = new ReportPeriod(LocalDate.of(2026, 8, 15), LocalDate.of(2026, 8, 15));

        assertThat(period.label()).isEqualTo("15.08.2026 – 15.08.2026");
    }

    @Test
    void fileSuffix_shouldBeIsoMonth_whenPeriodIsWholeMonth() {
        ReportPeriod period = ReportPeriod.ofMonth(YearMonth.of(2026, 8));

        assertThat(period.fileSuffix()).isEqualTo("2026-08");
    }

    @Test
    void fileSuffix_shouldBeFromAndToDates_whenPeriodIsNotWholeMonth() {
        ReportPeriod period = new ReportPeriod(LocalDate.of(2026, 8, 15), LocalDate.of(2026, 9, 10));

        assertThat(period.fileSuffix()).isEqualTo("2026-08-15_2026-09-10");
    }

    @Test
    void label_shouldNotTreatPartialMonthAsWholeMonth() {
        // Починається 1-го, але закінчується не в останній день місяця
        ReportPeriod period = new ReportPeriod(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 20));

        assertThat(period.label()).isEqualTo("01.08.2026 – 20.08.2026");
    }

    // --- resolve(month, from, to) ---

    @Test
    void resolve_shouldReturnWholeMonth_whenOnlyMonthGiven() {
        ReportPeriod period = ReportPeriod.resolve(YearMonth.of(2026, 8), null, null);

        assertThat(period).isEqualTo(ReportPeriod.ofMonth(YearMonth.of(2026, 8)));
    }

    @Test
    void resolve_shouldReturnRange_whenOnlyFromToGiven() {
        ReportPeriod period = ReportPeriod.resolve(
                null, LocalDate.of(2026, 8, 15), LocalDate.of(2026, 9, 10));

        assertThat(period).isEqualTo(new ReportPeriod(LocalDate.of(2026, 8, 15), LocalDate.of(2026, 9, 10)));
    }

    @Test
    void resolve_shouldThrow_whenMonthAndFromToBothGiven() {
        assertThatThrownBy(() -> ReportPeriod.resolve(
                YearMonth.of(2026, 8), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void resolve_shouldThrow_whenNothingGiven() {
        assertThatThrownBy(() -> ReportPeriod.resolve(null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void resolve_shouldThrow_whenOnlyFromGivenWithoutTo() {
        assertThatThrownBy(() -> ReportPeriod.resolve(null, LocalDate.of(2026, 8, 1), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void resolve_shouldThrow_whenOnlyToGivenWithoutFrom() {
        assertThatThrownBy(() -> ReportPeriod.resolve(null, null, LocalDate.of(2026, 8, 31)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void resolve_shouldThrow_whenFromAfterTo() {
        assertThatThrownBy(() -> ReportPeriod.resolve(
                null, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 8, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
