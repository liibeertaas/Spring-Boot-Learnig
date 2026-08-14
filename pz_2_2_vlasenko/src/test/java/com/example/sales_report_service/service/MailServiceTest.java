package com.example.sales_report_service.service;

import jakarta.mail.Address;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тести MailService: перевіряємо реально сформований MimeMessage (тема, отримувачі,
 * вкладення), а не факт відправки — сам SMTP-транспорт (JavaMailSender.send) замокано.
 */
class MailServiceTest {

    // Використовується лише для створення "живого" порожнього MimeMessage — без реального SMTP-з'єднання
    private final JavaMailSenderImpl mimeMessageFactory = new JavaMailSenderImpl();

    private JavaMailSender mailSender;
    private final List<String> recipients = List.of("boss1@example.com", "boss2@example.com");

    private MailService mailService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        when(mailSender.createMimeMessage()).thenAnswer(invocation -> mimeMessageFactory.createMimeMessage());
        mailService = new MailService(mailSender, recipients, "sender@example.com");
    }

    @Test
    void sendReport_shouldReturnRecipientsList() {
        List<String> result = mailService.sendReport(ReportPeriod.ofMonth(YearMonth.of(2026, 8)), "%PDF-".getBytes());

        assertThat(result).containsExactlyElementsOf(recipients);
    }

    @Test
    void sendReport_shouldSetCorrectSubjectWithUkrainianMonth() throws Exception {
        mailService.sendReport(ReportPeriod.ofMonth(YearMonth.of(2026, 8)), "%PDF-".getBytes());

        MimeMessage sentMessage = captureSentMessage();
        assertThat(sentMessage.getSubject()).isEqualTo("Звіт про продажі — Серпень 2026");
    }

    @Test
    void sendReport_shouldAddressAllRecipients() throws Exception {
        mailService.sendReport(ReportPeriod.ofMonth(YearMonth.of(2026, 8)), "%PDF-".getBytes());

        MimeMessage sentMessage = captureSentMessage();
        List<String> toAddresses = List.of(sentMessage.getAllRecipients()).stream()
                .map(Address::toString)
                .toList();

        assertThat(toAddresses).containsExactlyInAnyOrderElementsOf(recipients);
    }

    @Test
    void sendReport_shouldSetFromAddress() throws Exception {
        mailService.sendReport(ReportPeriod.ofMonth(YearMonth.of(2026, 8)), "%PDF-".getBytes());

        MimeMessage sentMessage = captureSentMessage();
        assertThat(sentMessage.getFrom()[0].toString()).isEqualTo("sender@example.com");
    }

    @Test
    void sendReport_shouldAttachPdfAsMultipart() throws Exception {
        mailService.sendReport(ReportPeriod.ofMonth(YearMonth.of(2026, 8)), "%PDF-content".getBytes());

        MimeMessage sentMessage = captureSentMessage();
        // getContentType() заголовок оновлюється лише при saveChanges() (це робить реальний транспорт
        // під час send(), якого тут немає — send() замокано). Тому перевіряємо сам вміст напряму.
        assertThat(sentMessage.getContent()).isInstanceOf(Multipart.class);

        Multipart multipart = (Multipart) sentMessage.getContent();
        assertThat(multipart.getBodyPart(1).getFileName()).isEqualTo("sales-report-2026-08.pdf");
    }

    @Test
    void sendReport_shouldUseDateRangeLabelAndFilename_forArbitraryPeriod() throws Exception {
        ReportPeriod arbitraryPeriod = new ReportPeriod(
                java.time.LocalDate.of(2026, 8, 15),
                java.time.LocalDate.of(2026, 9, 10)
        );

        mailService.sendReport(arbitraryPeriod, "%PDF-".getBytes());

        MimeMessage sentMessage = captureSentMessage();
        assertThat(sentMessage.getSubject()).isEqualTo("Звіт про продажі — 15.08.2026 – 10.09.2026");

        Multipart multipart = (Multipart) sentMessage.getContent();
        assertThat(multipart.getBodyPart(1).getFileName()).isEqualTo("sales-report-2026-08-15_2026-09-10.pdf");
    }

    @Test
    void sendReport_shouldPropagateMailException_whenSendFails() {
        doThrow(new MailSendException("SMTP недоступний")).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> mailService.sendReport(ReportPeriod.ofMonth(YearMonth.of(2026, 8)), "%PDF-".getBytes()))
                .isInstanceOf(MailException.class);
    }

    private MimeMessage captureSentMessage() {
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        return captor.getValue();
    }
}
