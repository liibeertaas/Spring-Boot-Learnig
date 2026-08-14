package com.example.sales_report_service.config;

import com.example.sales_report_service.model.Region;
import com.example.sales_report_service.model.Sale;
import com.example.sales_report_service.repository.SalesRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Наповнює in-memory сховище тестовими продажами при кожному старті застосунку —
 * щоб GET /sales, PDF/Excel-звіти й діаграма одразу мали що показати, а не були порожні.
 * Дані розкидані по кількох минулих місяцях (відносно сьогодні), щоб було видно і поточний,
 * і "порожній", і повний звітний місяць. Дані пропадають при перезапуску — сховище in-memory.
 */
@Component
@Order(1) // має відпрацювати ДО StartupReportMailer, щоб у звіті вже були дані
public class DataSeeder implements CommandLineRunner {

    private final SalesRepository salesRepository;

    public DataSeeder(SalesRepository salesRepository) {
        this.salesRepository = salesRepository;
    }

    @Override
    public void run(String... args) {
        LocalDate today = LocalDate.now();
        LocalDate lastMonth = today.minusMonths(1);
        LocalDate twoMonthsAgo = today.minusMonths(2);

        // Поточний місяць
        salesRepository.save(new Sale("Іван Петренко", "Ноутбук", new BigDecimal("12500.50"), Region.KYIV, today));
        salesRepository.save(new Sale("Іван Петренко", "Клавіатура", new BigDecimal("850.00"), Region.KYIV, today));
        salesRepository.save(new Sale("Олена Коваль", "Монітор", new BigDecimal("4300.00"), Region.LVIV, today));
        salesRepository.save(new Sale("Дмитро Шевченко", "Планшет", new BigDecimal("8900.00"), Region.KHARKIV, today));

        // Минулий місяць
        salesRepository.save(new Sale("Олена Коваль", "Мишка", new BigDecimal("320.75"), Region.DNIPRO, lastMonth));
        salesRepository.save(new Sale("Марія Бойко", "Принтер", new BigDecimal("6200.00"), Region.ODESA, lastMonth));
        salesRepository.save(new Sale("Марія Бойко", "Сканер", new BigDecimal("3100.00"), Region.ODESA, lastMonth));
        salesRepository.save(new Sale("Дмитро Шевченко", "Навушники", new BigDecimal("1450.00"), Region.KHARKIV, lastMonth));

        // Два місяці тому
        salesRepository.save(new Sale("Іван Петренко", "Веб-камера", new BigDecimal("990.00"), Region.KYIV, twoMonthsAgo));
        salesRepository.save(new Sale("Марія Бойко", "Флешка", new BigDecimal("250.00"), Region.ODESA, twoMonthsAgo));
    }
}
