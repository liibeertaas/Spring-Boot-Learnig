package com.example.pz_1_1_vlasenko.service;

import com.example.pz_1_1_vlasenko.model.AbsenceCategory;
import com.example.pz_1_1_vlasenko.model.AbsenceRecord;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class GoogleSheetsService {
    @Value("${app.google.spreadsheet-id}")
    private String spreadsheetId;

    @Value("${app.google.range}")
    private String range;

    public List<AbsenceRecord> fetchAbsenceData() {
        List<AbsenceRecord> records = new ArrayList<>();
        try {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new ClassPathResource("google-credentials.json").getInputStream())
                    .createScoped(Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY));

            Sheets sheetsService = new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("Student Absence Tracker")
                    .build();

            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                log.warn("No data found in Google Sheet.");
                return records;
            }

            // Парсимо дати з першого рядка (починаючи з 3-ї колонки, індекс 2)
            List<Object> header = values.get(0);
            List<LocalDate> dates = new ArrayList<>();
            for (int i = 2; i < header.size(); i++) {
                try {
                    dates.add(LocalDate.parse(header.get(i).toString(), DateTimeFormatter.ISO_LOCAL_DATE));
                } catch (DateTimeParseException e) {
                    dates.add(null); // Якщо не дата - пропускаємо
                }
            }

            // Парсимо студентів та їх відвідуваність
            for (int i = 1; i < values.size(); i++) {
                List<Object> row = values.get(i);
                if (row.size() < 2) continue; // Захист від порожніх рядків

                String group = row.get(0).toString().trim();
                String name = row.get(1).toString().trim();

                for (int colIndex = 2; colIndex < row.size(); colIndex++) {
                    if (colIndex - 2 < dates.size() && dates.get(colIndex - 2) != null) {
                        String cellVal = row.get(colIndex).toString();
                        AbsenceCategory category = AbsenceCategory.parse(cellVal);

                        if (category != AbsenceCategory.PRESENT) {
                            records.add(new AbsenceRecord(group, name, dates.get(colIndex - 2), category));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error reading Google Sheets: ", e);
        }
        return records;
    }
}
