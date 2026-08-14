package com.example.sales_report_service.service;

import com.example.sales_report_service.dto.SaleRequestDto;
import com.example.sales_report_service.dto.SaleResponseDto;
import com.example.sales_report_service.model.Region;
import com.example.sales_report_service.model.Sale;
import com.example.sales_report_service.repository.SalesRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Бізнес-логіка роботи з продажами: створення та фільтрація списку.
 */
@Service
public class SalesService {

    private final SalesRepository salesRepository;

    public SalesService(SalesRepository salesRepository) {
        this.salesRepository = salesRepository;
    }

    public SaleResponseDto addSale(SaleRequestDto dto) {
        Sale sale = dto.date() != null
                ? new Sale(dto.manager(), dto.product(), dto.amount(), dto.region(), dto.date())
                : new Sale(dto.manager(), dto.product(), dto.amount(), dto.region());
        salesRepository.save(sale);
        return SaleResponseDto.from(sale);
    }

    public List<SaleResponseDto> getSales(Region region, LocalDate from, LocalDate to) {
        return findSales(region, from, to).stream()
                .map(SaleResponseDto::from)
                .toList();
    }

    /**
     * Те саме фільтрування, що й getSales, але повертає доменні об'єкти Sale
     * (не DTO) — для внутрішніх споживачів на кшталт генерації PDF-звіту.
     */
    public List<Sale> findSales(Region region, LocalDate from, LocalDate to) {
        return salesRepository.findAll().stream()
                .filter(sale -> region == null || sale.getRegion() == region)
                .filter(sale -> from == null || !sale.getDate().isBefore(from))
                .filter(sale -> to == null || !sale.getDate().isAfter(to))
                .toList();
    }
}

