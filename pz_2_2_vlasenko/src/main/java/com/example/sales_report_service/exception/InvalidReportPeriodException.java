package com.example.sales_report_service.exception;

/**
 * Некоректна комбінація параметрів періоду звіту (month / from+to).
 */
public class InvalidReportPeriodException extends RuntimeException {

    public InvalidReportPeriodException(String message) {
        super(message);
    }
}
