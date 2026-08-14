package com.example.sales_report_service.dto;

import com.example.sales_report_service.model.Region;
import com.example.sales_report_service.model.Sale;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Дані про продаж, що повертаються клієнту (POST /sales відповідь, GET /sales список).
 */
public record SaleResponseDto(
        Long id,
        String manager,
        String product,
        BigDecimal amount,
        Region region,
        LocalDate date
) {

    public static SaleResponseDto from(Sale sale) {
        return new SaleResponseDto(
                sale.getId(),
                sale.getManager(),
                sale.getProduct(),
                sale.getAmount(),
                sale.getRegion(),
                sale.getDate()
        );
    }
}
