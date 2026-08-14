package com.example.sales_report_service.service;

import com.example.sales_report_service.model.Region;
import com.example.sales_report_service.model.Sale;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Обчислені підсумки для звіту про продажі: загальна сума, розбивка по регіонах,
 * рейтинг менеджерів за сумою продажів (спадання). Винесено окремо від PdfReportService,
 * щоб підрахунок можна було тестувати без генерації самого PDF.
 */
@Getter
public class SalesSummary {

    private final BigDecimal totalAmount;
    private final Map<Region, BigDecimal> totalsByRegion;
    private final List<ManagerTotal> managersRanked;

    private SalesSummary(BigDecimal totalAmount, Map<Region, BigDecimal> totalsByRegion, List<ManagerTotal> managersRanked) {
        this.totalAmount = totalAmount;
        this.totalsByRegion = totalsByRegion;
        this.managersRanked = managersRanked;
    }

    public static SalesSummary of(List<Sale> sales) {
        BigDecimal totalAmount = sales.stream()
                .map(Sale::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // TreeMap — регіони в підсумку йдуть у природному порядку enum (KYIV, LVIV, ...)
        Map<Region, BigDecimal> totalsByRegion = sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::getRegion,
                        TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Sale::getAmount, BigDecimal::add)
                ));

        List<ManagerTotal> managersRanked = sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::getManager,
                        Collectors.reducing(BigDecimal.ZERO, Sale::getAmount, BigDecimal::add)
                ))
                .entrySet().stream()
                .map(entry -> new ManagerTotal(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(ManagerTotal::total).reversed())
                .toList();

        return new SalesSummary(totalAmount, totalsByRegion, managersRanked);
    }
}
