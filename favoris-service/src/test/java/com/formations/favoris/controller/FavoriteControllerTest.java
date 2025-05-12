package com.formations.favoris.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formations.favoris.dto.AddFavoriteRequest;
import com.formations.favoris.dto.FavoriteDto;
import com.formations.favoris.service.FavoriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FavoriteController.class)
@ContextConfiguration(classes = com.formations.favoris_service.FavorisServiceApplication.class)
public class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavoriteService favoriteService;

    @Autowired
    private ObjectMapper objectMapper;

    private FavoriteDto testFavoriteDto;
    private AddFavoriteRequest validRequest;
    private AddFavoriteRequest invalidRequest;
    private final String userId = "user123";
    private final String contentId = "content456";
    private final String contentType = "COURSE";

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        testFavoriteDto = FavoriteDto.builder()
                .id("fav789")
                .userId(userId)
                .contentId(contentId)
                .contentType(contentType)
                .title("Test Course")
                .description("A test course description")
                .thumbnailUrl("http://example.com/thumbnail.jpg")
                .createdAt(now)
                .updatedAt(now)
                .build();

        validRequest = AddFavoriteRequest.builder()
                .userId(userId)
                .contentId(contentId)
                .contentType(contentType)
                .title("Test Course")
                .description("A test course description")
                .thumbnailUrl("http://example.com/thumbnail.jpg")
                .build();

        invalidRequest = AddFavoriteRequest.builder()
                .userId("")  // Champ vide pour tester la validation
                .contentId(contentId)
                .contentType(contentType)
                .title("")    // Champ vide pour tester la validation
                .build();
    }

    @Test
    @DisplayName("1. GET - Récupérer les favoris d'un utilisateur")
    void getUserFavorites_ShouldReturnFavorites() throws Exception {
        // Arrange
        List<FavoriteDto> favorites = Arrays.asList(testFavoriteDto);
        when(favoriteService.getUserFavorites(userId)).thenReturn(favorites);

        // Act & Assert
        mockMvc.perform(get("/api/favorites/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testFavoriteDto.getId())))
                .andExpect(jsonPath("$[0].title", is(testFavoriteDto.getTitle())));

        verify(favoriteService).getUserFavorites(userId);
    }

    @Test
    @DisplayName("1.1 GET - Récupérer les favoris d'un utilisateur sans favoris")
    void getUserFavorites_WhenNoFavorites_ShouldReturnEmptyList() throws Exception {
        // Arrange
        when(favoriteService.getUserFavorites(userId)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/favorites/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(favoriteService).getUserFavorites(userId);
    }

    @Test
    @DisplayName("1.2 GET - Récupérer les favoris par type de contenu")
    void getUserFavoritesByType_ShouldReturnFilteredFavorites() throws Exception {
        // Arrange
        List<FavoriteDto> favorites = Arrays.asList(testFavoriteDto);
        when(favoriteService.getUserFavoritesByType(userId, contentType)).thenReturn(favorites);

        // Act & Assert
        mockMvc.perform(get("/api/favorites/user/{userId}/type/{contentType}", userId, contentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].contentType", is(contentType)));

        verify(favoriteService).getUserFavoritesByType(userId, contentType);
    }

    @Test
    @DisplayName("2. POST - Ajouter un nouveau favori")
    void addFavorite_WithValidData_ShouldReturnCreated() throws Exception {
        // Arrange
        when(favoriteService.addFavorite(any(AddFavoriteRequest.class))).thenReturn(testFavoriteDto);

        // Act & Assert
        mockMvc.perform(post("/api/favorites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testFavoriteDto.getId())))
                .andExpect(jsonPath("$.title", is(testFavoriteDto.getTitle())));

        verify(favoriteService).addFavorite(any(AddFavoriteRequest.class));
    }

    @Test
    @DisplayName("2.1 POST - Ajouter un favori déjà existant")
    void addFavorite_WhenFavoriteExists_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(favoriteService.addFavorite(any(AddFavoriteRequest.class)))
                .thenThrow(new IllegalStateException("Ce contenu est déjà dans vos favoris"));

        // Act & Assert
        mockMvc.perform(post("/api/favorites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verify(favoriteService).addFavorite(any(AddFavoriteRequest.class));
    }

    @Test
    @DisplayName("3. DELETE - Supprimer un favori existant")
    void removeFavorite_WhenFavoriteExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        when(favoriteService.removeFavorite(userId, contentId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/favorites/user/{userId}/content/{contentId}", userId, contentId))
                .andExpect(status().isNoContent());

        verify(favoriteService).removeFavorite(userId, contentId);
    }

    @Test
    @DisplayName("3.1 DELETE - Supprimer un favori inexistant")
    void removeFavorite_WhenFavoriteDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(favoriteService.removeFavorite(userId, contentId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/favorites/user/{userId}/content/{contentId}", userId, contentId))
                .andExpect(status().isNotFound());

        verify(favoriteService).removeFavorite(userId, contentId);
    }

    @Test
    @DisplayName("4. Validation - Ajouter un favori avec données invalides")
    void addFavorite_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/favorites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(favoriteService, never()).addFavorite(any(AddFavoriteRequest.class));
    }

    @Test
    @DisplayName("5. POST - Toggle favori (ajout)")
    void toggleFavorite_WhenFavoriteDoesNotExist_ShouldAddAndReturnOk() throws Exception {
        // Arrange
        when(favoriteService.toggleFavorite(any(AddFavoriteRequest.class))).thenReturn(testFavoriteDto);

        // Act & Assert
        mockMvc.perform(post("/api/favorites/toggle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testFavoriteDto.getId())));

        verify(favoriteService).toggleFavorite(any(AddFavoriteRequest.class));
    }

    @Test
    @DisplayName("5.1 POST - Toggle favori (suppression)")
    void toggleFavorite_WhenFavoriteExists_ShouldRemoveAndReturnNoContent() throws Exception {
        // Arrange
        when(favoriteService.toggleFavorite(any(AddFavoriteRequest.class))).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/api/favorites/toggle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNoContent());

        verify(favoriteService).toggleFavorite(any(AddFavoriteRequest.class));
    }
}