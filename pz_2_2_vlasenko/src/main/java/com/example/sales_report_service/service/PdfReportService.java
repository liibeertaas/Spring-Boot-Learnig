package com.example.sales_report_service.service;

import com.example.sales_report_service.config.PdfFonts;
import com.example.sales_report_service.model.Region;
import com.example.sales_report_service.model.Sale;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Генерація PDF-звіту про продажі (таблиця + підсумки).
 * Сервіс не звертається до репозиторію — працює тільки з переданим йому списком продажів.
 */
@Service
public class PdfReportService {

    private static final Color HEADER_BACKGROUND = new Color(230, 230, 230);
    private static final Color SUMMARY_BACKGROUND = new Color(245, 245, 245);

    private final ChartService chartService;

    public PdfReportService(ChartService chartService) {
        this.chartService = chartService;
    }

    public byte[] generate(ReportPeriod period, List<Sale> sales) {
        try {
            SalesSummary summary = SalesSummary.of(sales);

            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);

            document.open();
            addTitle(document, period);
            addSalesTable(document, sales);
            addRegionChart(document, summary);
            addSummary(document, summary);
            document.close();

            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Не вдалося згенерувати PDF-звіт", e);
        }
    }

    private void addTitle(Document document, ReportPeriod period) throws DocumentException {
        Paragraph title = new Paragraph("Звіт про продажі", PdfFonts.TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph periodParagraph = new Paragraph("Період: " + period.label(), PdfFonts.NORMAL);
        periodParagraph.setAlignment(Element.ALIGN_CENTER);
        periodParagraph.setSpacingAfter(16f);
        document.add(periodParagraph);
    }

    private void addSalesTable(Document document, List<Sale> sales) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{2f, 3f, 3f, 2f, 3f});
        table.setWidthPercentage(100);
        table.setSpacingAfter(16f);

        for (String columnTitle : List.of("Дата", "Менеджер", "Товар", "Регіон", "Сума")) {
            table.addCell(headerCell(columnTitle));
        }

        // Сортування: спочатку за регіоном (порядок оголошення enum), потім за датою всередині регіону
        List<Sale> sorted = sales.stream()
                .sorted(Comparator.comparing(Sale::getRegion).thenComparing(Sale::getDate))
                .toList();

        for (Sale sale : sorted) {
            table.addCell(dataCell(sale.getDate().toString(), Element.ALIGN_CENTER));
            table.addCell(dataCell(sale.getManager(), Element.ALIGN_LEFT));
            table.addCell(dataCell(sale.getProduct(), Element.ALIGN_LEFT));
            table.addCell(dataCell(sale.getRegion().getDisplayName(), Element.ALIGN_CENTER));
            table.addCell(dataCell(formatAmount(sale.getAmount()), Element.ALIGN_RIGHT));
        }

        document.add(table);
    }

    private void addRegionChart(Document document, SalesSummary summary) throws DocumentException, IOException {
        if (summary.getTotalsByRegion().isEmpty()) {
            return; // немає продажів — немає що малювати
        }

        byte[] chartPng = chartService.generateRegionBarChartPng(summary.getTotalsByRegion());
        Image chartImage = Image.getInstance(chartPng);
        chartImage.setAlignment(Element.ALIGN_CENTER);
        chartImage.scaleToFit(480f, 300f);
        chartImage.setSpacingAfter(16f);
        document.add(chartImage);
    }

    private void addSummary(Document document, SalesSummary summary) throws DocumentException {
        Paragraph totalParagraph = new Paragraph("Загальна сума: " + formatAmount(summary.getTotalAmount()), PdfFonts.BOLD);
        totalParagraph.setSpacingAfter(10f);
        document.add(totalParagraph);

        document.add(new Paragraph("Розбивка по регіонах:", PdfFonts.HEADER));
        for (Map.Entry<Region, BigDecimal> entry : summary.getTotalsByRegion().entrySet()) {
            String line = entry.getKey().getDisplayName() + ": " + formatAmount(entry.getValue());
            document.add(new Paragraph(line, PdfFonts.NORMAL));
        }

        Paragraph spacer = new Paragraph(" ", PdfFonts.NORMAL);
        spacer.setSpacingAfter(6f);
        document.add(spacer);

        document.add(new Paragraph("Топ-продавці:", PdfFonts.HEADER));
        int place = 1;
        for (ManagerTotal managerTotal : summary.getManagersRanked()) {
            String line = place + ". " + managerTotal.manager() + " — " + formatAmount(managerTotal.total());
            document.add(new Paragraph(line, PdfFonts.NORMAL));
            place++;
        }
    }

    private PdfPCell headerCell(String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, PdfFonts.HEADER));
        cell.setBackgroundColor(HEADER_BACKGROUND);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6f);
        return cell;
    }

    private PdfPCell dataCell(String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, PdfFonts.NORMAL));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5f);
        return cell;
    }

    private String formatAmount(BigDecimal amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("uk", "UA"));
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator(' ');
        DecimalFormat format = new DecimalFormat("#,##0.00 ₴", symbols);
        return format.format(amount);
    }

}
