package com.example.sales_report_service.repository;

import com.example.sales_report_service.model.Sale;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сховище продажів у пам'яті (без БД).
 * Не містить бізнес-логіки (фільтрацію тощо) — тільки збереження і читання.
 */
@Repository
public class SalesRepository {

    private final ConcurrentHashMap<Long, Sale> storage = new ConcurrentHashMap<>();

    public Sale save(Sale sale) {
        storage.put(sale.getId(), sale);
        return sale;
    }

    public List<Sale> findAll() {
        return List.copyOf(storage.values());
    }
}
