package com.example.geo_places_service.controller;

import com.example.geo_places_service.exception.AddressNotFoundException;
import com.example.geo_places_service.exception.GeocodingUnavailableException;
import com.example.geo_places_service.service.GeocodingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** HTTP-шар зворотного геокодування (бонус) ізольовано, {@link GeocodingService} замінено моком. */
@WebMvcTest(GeocodingController.class)
class GeocodingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GeocodingService geocodingService;

    @Test
    void reverseReturns200WithAddress() throws Exception {
        when(geocodingService.reverseGeocode(48.8584, 2.2945))
                .thenReturn("Avenue Gustave Eiffel, Paris, France");

        mockMvc.perform(get("/api/geocode/reverse").param("lat", "48.8584").param("lon", "2.2945"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("Avenue Gustave Eiffel, Paris, France"))
                .andExpect(jsonPath("$.latitude").value(48.8584));
    }

    @Test
    void reverseReturns404WhenNothingFound() throws Exception {
        when(geocodingService.reverseGeocode(0.0, 0.0))
                .thenThrow(new AddressNotFoundException("0.0, 0.0"));

        mockMvc.perform(get("/api/geocode/reverse").param("lat", "0.0").param("lon", "0.0"))
                .andExpect(status().isNotFound());
    }

    @Test
    void reverseReturns503WhenNominatimUnavailable() throws Exception {
        when(geocodingService.reverseGeocode(48.8584, 2.2945))
                .thenThrow(new GeocodingUnavailableException("timeout", new RuntimeException()));

        mockMvc.perform(get("/api/geocode/reverse").param("lat", "48.8584").param("lon", "2.2945"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void reverseReturns400ForOutOfRangeLatitude() throws Exception {
        mockMvc.perform(get("/api/geocode/reverse").param("lat", "999").param("lon", "2.2945"))
                .andExpect(status().isBadRequest());
    }
}
