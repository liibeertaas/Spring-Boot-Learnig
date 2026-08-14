package com.example.sales_report_service.exception;

import java.util.Map;

/**
 * Уніфікований формат помилки, який повертає API.
 * fieldErrors — null, якщо помилка не пов'язана з конкретними полями.
 */
public record ErrorResponseDto(
        int status,
        String message,
        Map<String, String> fieldErrors
) {
}
