package com.example.sales_report_service.service;

import java.math.BigDecimal;

/**
 * Сумарний продаж одного менеджера за період — елемент рейтингу "топ-продавці".
 */
public record ManagerTotal(String manager, BigDecimal total) {
}
