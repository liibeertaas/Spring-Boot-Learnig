package com.example.pz_1_1_vlasenko.service;

import com.example.pz_1_1_vlasenko.model.AbsenceCategory;
import com.example.pz_1_1_vlasenko.model.AbsenceRecord;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportGeneratorService {
    public String generateHtmlReport(List<AbsenceRecord> records) {
        if (records.isEmpty()) {
            return "<h3>Відсутностей не знайдено! Усі присутні.</h3>";
        }

        StringBuilder html = new StringBuilder();
        html.append("<h2 style='color: #2c3e50;'>Звіт про відсутність студентів</h2>");

        // ЗНАХОДИМО ОСТАННЮ ДАТУ В ТАБЛИЦІ (щоб статистика працювала для тестових даних)
        LocalDate latestDateInData = records.stream()
                .map(AbsenceRecord::getDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

        int targetWeek = latestDateInData.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        int targetYear = latestDateInData.getYear();

        // Фільтруємо записи, щоб збігався рік та номер тижня
        List<AbsenceRecord> thisWeekRecords = records.stream()
                .filter(r -> r.getDate().getYear() == targetYear &&
                        r.getDate().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()) == targetWeek)
                .collect(Collectors.toList());

        html.append("<h3 style='color: #2980b9;'>Статистика за тиждень (включаючи ").append(latestDateInData).append("):</h3><ul>");
        Map<AbsenceCategory, Long> weekStats = thisWeekRecords.stream()
                .collect(Collectors.groupingBy(AbsenceRecord::getCategory, Collectors.counting()));

        for (AbsenceCategory cat : AbsenceCategory.values()) {
            if (cat != AbsenceCategory.PRESENT) {
                html.append("<li><b>").append(cat.getTitle()).append(":</b> ")
                        .append(weekStats.getOrDefault(cat, 0L)).append("</li>");
            }
        }
        html.append("</ul><hr/>");

        // Загальні списки по групах та категоріях
        html.append("<h3 style='color: #8e44ad;'>Детальний список відсутніх (за весь час):</h3>");

        Map<String, Map<AbsenceCategory, List<AbsenceRecord>>> groupedByGroupAndCat = records.stream()
                .collect(Collectors.groupingBy(
                        AbsenceRecord::getGroup,
                        Collectors.groupingBy(AbsenceRecord::getCategory)
                ));

        for (Map.Entry<String, Map<AbsenceCategory, List<AbsenceRecord>>> groupEntry : groupedByGroupAndCat.entrySet()) {
            html.append("<h4 style='background-color: #ecf0f1; padding: 5px;'>Група: ").append(groupEntry.getKey()).append("</h4>");

            for (Map.Entry<AbsenceCategory, List<AbsenceRecord>> catEntry : groupEntry.getValue().entrySet()) {
                html.append("<p><b>").append(catEntry.getKey().getTitle()).append(":</b></p><ul>");

                // Групуємо дати для кожного студента
                Map<String, List<LocalDate>> studentDates = catEntry.getValue().stream()
                        .collect(Collectors.groupingBy(AbsenceRecord::getFullName,
                                Collectors.mapping(AbsenceRecord::getDate, Collectors.toList())));

                for (Map.Entry<String, List<LocalDate>> studentEntry : studentDates.entrySet()) {
                    html.append("<li>").append(studentEntry.getKey()).append(" (дати: ");
                    String dates = studentEntry.getValue().stream()
                            .map(LocalDate::toString)
                            .collect(Collectors.joining(", "));
                    html.append(dates).append(")</li>");
                }
                html.append("</ul>");
            }
        }
        return html.toString();
    }
}
