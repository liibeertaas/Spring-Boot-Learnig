package com.example.sales_report_service.dto;

import com.example.sales_report_service.model.Region;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Дані для створення нового продажу (POST /sales).
 * id не передається клієнтом — його проставляє сервер.
 * date — опційне: якщо не вказане, сервер проставляє сьогоднішню дату.
 */
public record SaleRequestDto(

        @NotBlank(message = "Поле manager не може бути порожнім")
        String manager,

        @NotBlank(message = "Поле product не може бути порожнім")
        String product,

        @NotNull(message = "Поле amount є обов'язковим")
        @PositiveOrZero(message = "Поле amount не може бути від'ємним")
        BigDecimal amount,

        @NotNull(message = "Поле region є обов'язковим")
        Region region,

        @PastOrPresent(message = "Поле date не може бути в майбутньому")
        LocalDate date

) {
}
