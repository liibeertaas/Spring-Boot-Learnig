package com.example.pz_1_1_vlasenko.service;

import com.example.pz_1_1_vlasenko.model.AbsenceRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {
    private final GoogleSheetsService sheetsService;
    private final ReportGeneratorService reportGenerator;
    private final EmailService emailService;

    @Scheduled(cron = "${app.report.cron}")
    public void executeAbsenceReportJob() {
        log.info("Starting scheduled absence report job...");

        List<AbsenceRecord> records = sheetsService.fetchAbsenceData();
        if (records.isEmpty()) {
            log.info("No absences to report.");
            // Можна відправляти лист, що всі присутні, або не відправляти нічого
        }

        String htmlReport = reportGenerator.generateHtmlReport(records);
        emailService.sendHtmlEmail("Щоденний звіт про відсутність студентів", htmlReport);

        log.info("Scheduled job completed.");
    }
}
