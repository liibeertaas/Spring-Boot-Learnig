package com.example.sales_report_service.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Доменна модель продажу. Об'єкт незмінний (immutable) — без сетерів.
 * id генерується автоматично (лічильник усередині класу).
 * date — сьогодні за замовчуванням, або довільна дата, якщо передана явно.
 */
@Getter
public class Sale {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);

    private final Long id;
    private final String manager;
    private final String product;
    private final BigDecimal amount;
    private final Region region;
    private final LocalDate date;

    public Sale(String manager, String product, BigDecimal amount, Region region) {
        this(manager, product, amount, region, LocalDate.now());
    }

    public Sale(String manager, String product, BigDecimal amount, Region region, LocalDate date) {
        this.id = ID_GENERATOR.incrementAndGet();
        this.manager = manager;
        this.product = product;
        this.amount = amount;
        this.region = region;
        this.date = date;
    }
}
