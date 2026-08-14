package com.example.sales_report_service.service;

import com.example.sales_report_service.model.Region;
import com.example.sales_report_service.model.Sale;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тести генерації Excel-звіту: перевіряємо реальний вміст .xlsx через Apache POI
 * (заголовки, порядок сортування, коректність кирилиці, числовий тип суми).
 */
class ExcelReportServiceTest {

    private final ExcelReportService excelReportService = new ExcelReportService();

    @Test
    void generate_shouldReturnNonEmptyByteArray() {
        byte[] excel = excelReportService.generate(List.of());

        assertThat(excel).isNotEmpty();
    }

    @Test
    void generate_shouldContainCorrectHeaderRow() throws IOException {
        byte[] excel = excelReportService.generate(List.of());

        try (Workbook workbook = readWorkbook(excel)) {
            Row header = workbook.getSheetAt(0).getRow(0);

            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Дата");
            assertThat(header.getCell(1).getStringCellValue()).isEqualTo("Менеджер");
            assertThat(header.getCell(2).getStringCellValue()).isEqualTo("Товар");
            assertThat(header.getCell(3).getStringCellValue()).isEqualTo("Регіон");
            assertThat(header.getCell(4).getStringCellValue()).isEqualTo("Сума");
        }
    }

    @Test
    void generate_shouldContainOnlyHeaderRow_whenNoSales() throws IOException {
        byte[] excel = excelReportService.generate(List.of());

        try (Workbook workbook = readWorkbook(excel)) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getLastRowNum()).isZero();
        }
    }

    @Test
    void generate_shouldSortByRegionThenDate() throws IOException {
        List<Sale> sales = List.of(
                new Sale("Manager B", "Product", new BigDecimal("20.00"), Region.LVIV),
                new Sale("Manager A", "Product", new BigDecimal("10.00"), Region.KYIV)
        );

        byte[] excel = excelReportService.generate(sales);

        try (Workbook workbook = readWorkbook(excel)) {
            Sheet sheet = workbook.getSheetAt(0);

            assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("Manager A");
            assertThat(sheet.getRow(1).getCell(3).getStringCellValue()).isEqualTo("Київ");
            assertThat(sheet.getRow(2).getCell(1).getStringCellValue()).isEqualTo("Manager B");
            assertThat(sheet.getRow(2).getCell(3).getStringCellValue()).isEqualTo("Львів");
        }
    }

    @Test
    void generate_shouldPreserveCyrillicText() throws IOException {
        List<Sale> sales = List.of(
                new Sale("Іван Петренко", "Ноутбук", new BigDecimal("100.00"), Region.KYIV)
        );

        byte[] excel = excelReportService.generate(sales);

        try (Workbook workbook = readWorkbook(excel)) {
            Row row = workbook.getSheetAt(0).getRow(1);
            assertThat(row.getCell(1).getStringCellValue()).isEqualTo("Іван Петренко");
            assertThat(row.getCell(2).getStringCellValue()).isEqualTo("Ноутбук");
        }
    }

    @Test
    void generate_shouldWriteAmountAsNumericCell() throws IOException {
        List<Sale> sales = List.of(
                new Sale("Manager", "Product", new BigDecimal("123.45"), Region.KYIV)
        );

        byte[] excel = excelReportService.generate(sales);

        try (Workbook workbook = readWorkbook(excel)) {
            Cell amountCell = workbook.getSheetAt(0).getRow(1).getCell(4);
            assertThat(amountCell.getCellType()).isEqualTo(CellType.NUMERIC);
            assertThat(amountCell.getNumericCellValue()).isEqualTo(123.45);
        }
    }

    private Workbook readWorkbook(byte[] excel) throws IOException {
        return WorkbookFactory.create(new ByteArrayInputStream(excel));
    }
}
