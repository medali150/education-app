package com.formations.favoris.service;

import com.formations.favoris.dto.AddFavoriteRequest;
import com.formations.favoris.dto.FavoriteDto;
import com.formations.favoris.model.Favorite;
import com.formations.favoris.repository.FavoriteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    private Favorite testFavorite;
    private AddFavoriteRequest testRequest;
    private final String userId = "user123";
    private final String contentId = "content456";
    private final String contentType = "COURSE";

    @BeforeEach
    void setUp() {
        // Configuration des objets de test
        LocalDateTime now = LocalDateTime.now();
        
        testFavorite = Favorite.builder()
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

        testRequest = AddFavoriteRequest.builder()
                .userId(userId)
                .contentId(contentId)
                .contentType(contentType)
                .title("Test Course")
                .description("A test course description")
                .thumbnailUrl("http://example.com/thumbnail.jpg")
                .build();
    }

    @Test
    @DisplayName("1. Récupération des favoris d'un utilisateur")
    void getUserFavorites_ShouldReturnUserFavorites() {
        // Arrange
        when(favoriteRepository.findByUserId(userId))
                .thenReturn(Arrays.asList(testFavorite));

        // Act
        List<FavoriteDto> result = favoriteService.getUserFavorites(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testFavorite.getId(), result.get(0).getId());
        assertEquals(testFavorite.getTitle(), result.get(0).getTitle());
        verify(favoriteRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("1.1 Récupération des favoris - utilisateur sans favoris")
    void getUserFavorites_WhenUserHasNoFavorites_ShouldReturnEmptyList() {
        // Arrange
        when(favoriteRepository.findByUserId(userId))
                .thenReturn(Collections.emptyList());

        // Act
        List<FavoriteDto> result = favoriteService.getUserFavorites(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(favoriteRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("1.2 Récupération des favoris par type de contenu")
    void getUserFavoritesByType_ShouldReturnFilteredFavorites() {
        // Arrange
        when(favoriteRepository.findByUserIdAndContentType(userId, contentType))
                .thenReturn(Arrays.asList(testFavorite));

        // Act
        List<FavoriteDto> result = favoriteService.getUserFavoritesByType(userId, contentType);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(contentType, result.get(0).getContentType());
        verify(favoriteRepository).findByUserIdAndContentType(userId, contentType);
    }

    @Test
    @DisplayName("2. Ajout d'un nouveau favori")
    void addFavorite_ShouldAddNewFavorite() {
        // Arrange
        when(favoriteRepository.existsByUserIdAndContentId(userId, contentId))
                .thenReturn(false);
        when(favoriteRepository.save(any(Favorite.class)))
                .thenReturn(testFavorite);

        // Act
        FavoriteDto result = favoriteService.addFavorite(testRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testFavorite.getId(), result.getId());
        assertEquals(testFavorite.getTitle(), result.getTitle());
        verify(favoriteRepository).existsByUserIdAndContentId(userId, contentId);
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    @DisplayName("3. Gestion des doublons lors de l'ajout")
    void addFavorite_WhenFavoriteAlreadyExists_ShouldThrowException() {
        // Arrange
        when(favoriteRepository.existsByUserIdAndContentId(userId, contentId))
                .thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            favoriteService.addFavorite(testRequest);
        });
        
        assertEquals("Ce contenu est déjà dans vos favoris", exception.getMessage());
        verify(favoriteRepository).existsByUserIdAndContentId(userId, contentId);
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    @DisplayName("4. Suppression d'un favori existant")
    void removeFavorite_WhenFavoriteExists_ShouldReturnTrue() {
        // Arrange
        when(favoriteRepository.findByUserIdAndContentId(userId, contentId))
                .thenReturn(Optional.of(testFavorite));
        doNothing().when(favoriteRepository).deleteByUserIdAndContentId(userId, contentId);

        // Act
        boolean result = favoriteService.removeFavorite(userId, contentId);

        // Assert
        assertTrue(result);
        verify(favoriteRepository).findByUserIdAndContentId(userId, contentId);
        verify(favoriteRepository).deleteByUserIdAndContentId(userId, contentId);
    }

    @Test
    @DisplayName("5. Tentative de suppression d'un favori inexistant")
    void removeFavorite_WhenFavoriteDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(favoriteRepository.findByUserIdAndContentId(userId, contentId))
                .thenReturn(Optional.empty());

        // Act
        boolean result = favoriteService.removeFavorite(userId, contentId);

        // Assert
        assertFalse(result);
        verify(favoriteRepository).findByUserIdAndContentId(userId, contentId);
        verify(favoriteRepository, never()).deleteByUserIdAndContentId(any(), any());
    }

    @Test
    @DisplayName("6.1 Toggle - Ajout d'un favori inexistant")
    void toggleFavorite_WhenFavoriteDoesNotExist_ShouldAddFavorite() {
        // Arrange
        when(favoriteRepository.findByUserIdAndContentId(userId, contentId))
                .thenReturn(Optional.empty());
        when(favoriteRepository.existsByUserIdAndContentId(userId, contentId))
                .thenReturn(false);
        when(favoriteRepository.save(any(Favorite.class)))
                .thenReturn(testFavorite);

        // Act
        FavoriteDto result = favoriteService.toggleFavorite(testRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testFavorite.getId(), result.getId());
        verify(favoriteRepository).findByUserIdAndContentId(userId, contentId);
        verify(favoriteRepository).existsByUserIdAndContentId(userId, contentId);
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    @DisplayName("6.2 Toggle - Suppression d'un favori existant")
    void toggleFavorite_WhenFavoriteExists_ShouldRemoveFavorite() {
        // Arrange
        when(favoriteRepository.findByUserIdAndContentId(userId, contentId))
                .thenReturn(Optional.of(testFavorite));
        doNothing().when(favoriteRepository).deleteByUserIdAndContentId(userId, contentId);

        // Act
        FavoriteDto result = favoriteService.toggleFavorite(testRequest);

        // Assert
        assertNull(result);
        verify(favoriteRepository).findByUserIdAndContentId(userId, contentId);
        verify(favoriteRepository).deleteByUserIdAndContentId(userId, contentId);
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }
}