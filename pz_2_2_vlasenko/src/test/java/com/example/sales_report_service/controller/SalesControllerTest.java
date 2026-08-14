package com.example.sales_report_service.controller;

import com.example.sales_report_service.dto.SaleRequestDto;
import com.example.sales_report_service.dto.SaleResponseDto;
import com.example.sales_report_service.model.Region;
import com.example.sales_report_service.service.SalesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тести HTTP-шару SalesController: статуси відповідей, валідація, обробка помилок.
 * SalesService замінюється мок-бином — тестується тільки веб-шар (контролер + GlobalExceptionHandler).
 */
@WebMvcTest(SalesController.class)
class SalesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SalesService salesService;

    @Test
    void addSale_shouldReturn201_whenRequestValid() throws Exception {
        SaleRequestDto request = new SaleRequestDto("Manager", "Product", new BigDecimal("100.00"), Region.KYIV, null);
        SaleResponseDto response = new SaleResponseDto(
                1L, "Manager", "Product", new BigDecimal("100.00"), Region.KYIV, LocalDate.now());

        when(salesService.addSale(any())).thenReturn(response);

        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.manager").value("Manager"));
    }

    @Test
    void addSale_shouldReturn400_whenManagerBlank() throws Exception {
        String json = """
                {"manager":"","product":"Product","amount":100,"region":"KYIV"}
                """;

        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.manager").exists());
    }

    @Test
    void addSale_shouldReturn400_whenAmountNegative() throws Exception {
        String json = """
                {"manager":"Manager","product":"Product","amount":-10,"region":"KYIV"}
                """;

        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.amount").exists());
    }

    @Test
    void addSale_shouldReturn400_whenRegionInvalid() throws Exception {
        String json = """
                {"manager":"Manager","product":"Product","amount":10,"region":"PARIS"}
                """;

        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addSale_shouldAcceptExplicitPastDate() throws Exception {
        String json = """
                {"manager":"Manager","product":"Product","amount":100,"region":"KYIV","date":"2026-06-01"}
                """;
        SaleResponseDto response = new SaleResponseDto(
                1L, "Manager", "Product", new BigDecimal("100.00"), Region.KYIV, LocalDate.of(2026, 6, 1));
        when(salesService.addSale(any())).thenReturn(response);

        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.date").value("2026-06-01"));
    }

    @Test
    void addSale_shouldReturn400_whenDateInFuture() throws Exception {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        String json = """
                {"manager":"Manager","product":"Product","amount":100,"region":"KYIV","date":"%s"}
                """.formatted(futureDate);

        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.date").exists());
    }

    @Test
    void getSales_shouldReturn200AndList() throws Exception {
        SaleResponseDto sale = new SaleResponseDto(
                1L, "Manager", "Product", new BigDecimal("100.00"), Region.KYIV, LocalDate.now());
        when(salesService.getSales(null, null, null)).thenReturn(List.of(sale));

        mockMvc.perform(get("/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].manager").value("Manager"));
    }

    @Test
    void getSales_shouldReturn400_whenRegionParamInvalid() throws Exception {
        mockMvc.perform(get("/sales").param("region", "PARIS"))
                .andExpect(status().isBadRequest());
    }
}
