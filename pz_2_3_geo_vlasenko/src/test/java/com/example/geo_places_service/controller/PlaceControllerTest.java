package com.example.geo_places_service.controller;

import com.example.geo_places_service.dto.NearbyPlaceResponse;
import com.example.geo_places_service.dto.PlaceCreateRequest;
import com.example.geo_places_service.dto.PlaceResponse;
import com.example.geo_places_service.dto.PlaceUpdateRequest;
import com.example.geo_places_service.exception.PlaceNotFoundException;
import com.example.geo_places_service.service.PlaceService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP-шар {@link PlaceController} ізольовано від сервісу/БД: {@link PlaceService}
 * замінено моком. Перевіряємо коди відповіді, валідацію запиту й те, що
 * {@link com.example.geo_places_service.exception.GlobalExceptionHandler} справді
 * перетворює винятки сервісу на очікувані статуси (а не 500).
 */
@WebMvcTest(PlaceController.class)
class PlaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlaceService placeService;

    private PlaceResponse samplePlace(Long id) {
        return new PlaceResponse(id, "Cafe Central", "Rynok 1, Lviv", "cafe", 49.84, 24.03,
                true, null, LocalDateTime.now());
    }

    @Test
    void createReturns201WithLocationHeader() throws Exception {
        PlaceCreateRequest request = new PlaceCreateRequest("Cafe Central", "Rynok 1, Lviv", "cafe", 49.84, 24.03);
        when(placeService.create(any(PlaceCreateRequest.class))).thenReturn(samplePlace(1L));

        mockMvc.perform(post("/api/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/places/1"))
                .andExpect(jsonPath("$.name").value("Cafe Central"))
                .andExpect(jsonPath("$.geocoded").value(true));
    }

    @Test
    void createWithBlankNameReturns400WithFieldErrors() throws Exception {
        String body = """
                {"name":"","address":"some address"}
                """;

        mockMvc.perform(post("/api/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void createWithInvalidLatitudeReturns400() throws Exception {
        String body = """
                {"name":"X","address":"Y","latitude":999.0,"longitude":24.0}
                """;

        mockMvc.perform(post("/api/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByIdReturns200WhenFound() throws Exception {
        when(placeService.getById(1L)).thenReturn(samplePlace(1L));

        mockMvc.perform(get("/api/places/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.category").value("cafe"));
    }

    @Test
    void getByIdReturns404WhenServiceThrowsNotFound() throws Exception {
        when(placeService.getById(999L)).thenThrow(new PlaceNotFoundException(999L));

        mockMvc.perform(get("/api/places/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateReturns200WithUpdatedFields() throws Exception {
        PlaceUpdateRequest request = new PlaceUpdateRequest("New name", "New address", "cafe", 1.0, 2.0);
        when(placeService.update(eq(1L), any(PlaceUpdateRequest.class))).thenReturn(samplePlace(1L));

        mockMvc.perform(put("/api/places/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/places/1"))
                .andExpect(status().isNoContent());

        verify(placeService).delete(1L);
    }

    @Test
    void deleteReturns404WhenPlaceNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new PlaceNotFoundException(999L)).when(placeService).delete(999L);

        mockMvc.perform(delete("/api/places/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void regeocodeReturns200() throws Exception {
        when(placeService.regeocode(1L)).thenReturn(samplePlace(1L));

        mockMvc.perform(post("/api/places/1/regeocode"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(placeService).regeocode(1L);
    }

    @Test
    void listPassesFiltersAndPagingToService() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<PlaceResponse> page = new PageImpl<>(List.of(samplePlace(1L)), pageable, 1);
        when(placeService.list(eq("cafe"), eq("central"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/places").param("category", "cafe").param("name", "central"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));

        verify(placeService).list(eq("cafe"), eq("central"), any(Pageable.class));
    }

    @Test
    void nearbyReturns200ForValidCoordinates() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        NearbyPlaceResponse nearby = new NearbyPlaceResponse(1L, "Cafe Central", "Rynok 1, Lviv", "cafe", 49.84, 24.03, 0.5);
        when(placeService.findNearby(eq(49.84), eq(24.03), eq(5.0), eq((String) null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(nearby), pageable, 1));

        mockMvc.perform(get("/api/places/nearby")
                        .param("lat", "49.84")
                        .param("lon", "24.03")
                        .param("radiusKm", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].distanceKm").value(0.5));
    }

    @Test
    void nearbyReturns400WhenLatitudeOutOfRange() throws Exception {
        mockMvc.perform(get("/api/places/nearby")
                        .param("lat", "999")
                        .param("lon", "24.03")
                        .param("radiusKm", "5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nearbyReturns400WhenRadiusIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/places/nearby")
                        .param("lat", "49.84")
                        .param("lon", "24.03")
                        .param("radiusKm", "0"))
                .andExpect(status().isBadRequest());
    }
}
