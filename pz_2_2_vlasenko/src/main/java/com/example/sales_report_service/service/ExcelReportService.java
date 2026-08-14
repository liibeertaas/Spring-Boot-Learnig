package com.example.sales_report_service.service;

import com.example.sales_report_service.model.Sale;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/**
 * Генерація звіту про продажі у форматі Excel (.xlsx) — таблиця продажів,
 * без підсумків (ті є в PDF-звіті).
 */
@Service
public class ExcelReportService {

    private static final String[] HEADERS = {"Дата", "Менеджер", "Товар", "Регіон", "Сума"};

    public byte[] generate(List<Sale> sales) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Продажі");
            setColumnWidths(sheet);

            writeHeaderRow(sheet, createHeaderStyle(workbook));
            writeDataRows(sheet, sales);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Не вдалося згенерувати Excel-звіт", e);
        }
    }

    private void setColumnWidths(Sheet sheet) {
        int[] widths = {12, 25, 25, 12, 15};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
    }

    private void writeHeaderRow(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void writeDataRows(Sheet sheet, List<Sale> sales) {
        // Той самий порядок сортування, що й у PDF: спочатку регіон, потім дата
        List<Sale> sorted = sales.stream()
                .sorted(Comparator.comparing(Sale::getRegion).thenComparing(Sale::getDate))
                .toList();

        int rowIndex = 1;
        for (Sale sale : sorted) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(sale.getDate().toString());
            row.createCell(1).setCellValue(sale.getManager());
            row.createCell(2).setCellValue(sale.getProduct());
            row.createCell(3).setCellValue(sale.getRegion().getDisplayName());
            row.createCell(4).setCellValue(sale.getAmount().doubleValue());
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(boldFont);
        return style;
    }
}
