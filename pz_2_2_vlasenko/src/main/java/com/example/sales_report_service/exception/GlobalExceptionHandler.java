package com.example.sales_report_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Централізована обробка помилок API — перетворює винятки на коректні
 * HTTP-коди й читабельні повідомлення (замість технічних стектрейсів).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Помилки валідації @Valid у @RequestBody (напр. порожній manager, amount < 0)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        ErrorResponseDto body = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Помилка валідації вхідних даних",
                fieldErrors
        );
        return ResponseEntity.badRequest().body(body);
    }

    // Некоректне тіло запиту: битий JSON або невалідне значення enum (напр. "region": "PARIS")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleUnreadable(HttpMessageNotReadableException ex) {
        ErrorResponseDto body = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Некоректне тіло запиту: перевірте формат JSON і допустимі значення полів (region, amount тощо)",
                null
        );
        return ResponseEntity.badRequest().body(body);
    }

    // Відсутній обов'язковий query-параметр (напр. GET /reports/sales.pdf без ?month=...)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingParam(MissingServletRequestParameterException ex) {
        ErrorResponseDto body = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Відсутній обов'язковий параметр '" + ex.getParameterName() + "'",
                null
        );
        return ResponseEntity.badRequest().body(body);
    }

    // Некоректна комбінація параметрів періоду звіту (month / from+to)
    @ExceptionHandler(InvalidReportPeriodException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidPeriod(InvalidReportPeriodException ex) {
        ErrorResponseDto body = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                null
        );
        return ResponseEntity.badRequest().body(body);
    }

    // Некоректний query-параметр у GET /sales (напр. ?region=PARIS або ?from=не-дата)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ErrorResponseDto body = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Некоректне значення параметра '" + ex.getName() + "'",
                null
        );
        return ResponseEntity.badRequest().body(body);
    }

    // Будь-яка інша непередбачена помилка
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneric(Exception ex) {
        ErrorResponseDto body = new ErrorResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Внутрішня помилка сервера",
                null
        );
        return ResponseEntity.internalServerError().body(body);
    }
}
