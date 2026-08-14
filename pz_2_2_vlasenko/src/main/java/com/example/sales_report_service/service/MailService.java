package com.example.sales_report_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Формування та надсилання листа зі звітом про продажі (PDF вкладенням).
 */
@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final List<String> recipients;
    private final String fromAddress;

    public MailService(
            JavaMailSender mailSender,
            @Value("${report.recipients}") List<String> recipients,
            @Value("${spring.mail.username}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.recipients = recipients;
        this.fromAddress = fromAddress;
    }

    public List<String> sendReport(ReportPeriod period, byte[] pdfBytes) {
        return sendReport(period, pdfBytes, null);
    }

    /**
     * @param excelBytes опційне вкладення .xlsx (може бути null — тоді лист лише з PDF)
     */
    public List<String> sendReport(ReportPeriod period, byte[] pdfBytes, byte[] excelBytes) {
        String periodLabel = period.label();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(recipients.toArray(new String[0]));
            helper.setSubject("Звіт про продажі — " + periodLabel);
            helper.setText("У вкладенні — звіт про продажі за " + periodLabel + ".", false);
            helper.addAttachment("sales-report-" + period.fileSuffix() + ".pdf", new ByteArrayResource(pdfBytes));

            if (excelBytes != null) {
                helper.addAttachment("sales-report-" + period.fileSuffix() + ".xlsx", new ByteArrayResource(excelBytes));
            }

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Не вдалося надіслати email зі звітом", e);
        }

        return recipients;
    }
}
