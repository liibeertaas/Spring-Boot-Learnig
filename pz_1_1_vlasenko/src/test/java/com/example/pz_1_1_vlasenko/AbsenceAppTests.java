package com.example.pz_1_1_vlasenko;

import com.example.pz_1_1_vlasenko.model.AbsenceCategory;
import com.example.pz_1_1_vlasenko.model.AbsenceRecord;
import com.example.pz_1_1_vlasenko.service.ReportGeneratorService;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AbsenceAppTests {
    @Test
    void testAbsenceCategoryParsing() {
        assertEquals(AbsenceCategory.SICK, AbsenceCategory.parse("х"));
        assertEquals(AbsenceCategory.SICK, AbsenceCategory.parse(" ХВ "));
        assertEquals(AbsenceCategory.MEDICAL, AbsenceCategory.parse("сч"));
        assertEquals(AbsenceCategory.BUSINESS_TRIP, AbsenceCategory.parse("вд"));
        assertEquals(AbsenceCategory.VACATION, AbsenceCategory.parse("вп"));
        assertEquals(AbsenceCategory.PRESENT, AbsenceCategory.parse(""));
        assertEquals(AbsenceCategory.PRESENT, AbsenceCategory.parse(null));
        assertEquals(AbsenceCategory.PRESENT, AbsenceCategory.parse("invalid_code"));
    }

    @Test
    void testReportGenerationHtml() {
        ReportGeneratorService reportService = new ReportGeneratorService();
        List<AbsenceRecord> records = List.of(
                new AbsenceRecord("КН-11", "Іваненко Іван", LocalDate.now(), AbsenceCategory.SICK),
                new AbsenceRecord("КН-11", "Петренко Петро", LocalDate.now().minusDays(1), AbsenceCategory.VACATION),
                new AbsenceRecord("КН-12", "Сидоренко Сидір", LocalDate.now(), AbsenceCategory.SICK)
        );

        String html = reportService.generateHtmlReport(records);

        // Перевіряємо чи згенеровано групи та категорії
        assertTrue(html.contains("КН-11"));
        assertTrue(html.contains("КН-12"));
        assertTrue(html.contains("Іваненко Іван"));
        assertTrue(html.contains("Сидоренко Сидір"));
        assertTrue(html.contains(AbsenceCategory.SICK.getTitle()));
    }
}
