package com.example.sales_report_service.controller;

import com.example.sales_report_service.dto.SaleRequestDto;
import com.example.sales_report_service.dto.SaleResponseDto;
import com.example.sales_report_service.model.Region;
import com.example.sales_report_service.service.SalesService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/sales")
public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    @PostMapping
    public ResponseEntity<SaleResponseDto> addSale(@Valid @RequestBody SaleRequestDto dto) {
        SaleResponseDto created = salesService.addSale(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<SaleResponseDto>> getSales(
            @RequestParam(required = false) Region region,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(salesService.getSales(region, from, to));
    }
}
