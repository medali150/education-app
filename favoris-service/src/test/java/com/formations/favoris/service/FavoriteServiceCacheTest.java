package com.formations.favoris.service;

import com.formations.favoris.dto.AddFavoriteRequest;
import com.formations.favoris.dto.FavoriteDto;
import com.formations.favoris.model.Favorite;
import com.formations.favoris.repository.FavoriteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ContextConfiguration(classes = com.formations.favoris_service.FavorisServiceApplication.class)
@TestPropertySource(properties = {
    "spring.cache.type=simple",  // Utiliser un cache en mémoire pour les tests
    "spring.data.redis.port=6370"  // Port différent pour éviter les conflits
})
public class FavoriteServiceCacheTest {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private FavoriteRepository favoriteRepository;

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

        // Vider le cache avant chaque test
        cacheManager.getCacheNames().forEach(cacheName -> {
            cacheManager.getCache(cacheName).clear();
        });
    }

    @Test
    @DisplayName("Test du cache pour getUserFavorites")
    void getUserFavorites_ShouldUseCacheOnSecondCall() {
        // Arrange
        when(favoriteRepository.findByUserId(userId))
                .thenReturn(Arrays.asList(testFavorite));

        // Act - Premier appel (miss de cache)
        favoriteService.getUserFavorites(userId);
        
        // Act - Deuxième appel (hit de cache)
        favoriteService.getUserFavorites(userId);

        // Assert - Le repository ne doit être appelé qu'une seule fois
        verify(favoriteRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("Test du cache pour getUserFavoritesByType")
    void getUserFavoritesByType_ShouldUseCacheOnSecondCall() {
        // Arrange
        when(favoriteRepository.findByUserIdAndContentType(userId, contentType))
                .thenReturn(Arrays.asList(testFavorite));

        // Act - Premier appel (miss de cache)
        favoriteService.getUserFavoritesByType(userId, contentType);
        
        // Act - Deuxième appel (hit de cache)
        favoriteService.getUserFavoritesByType(userId, contentType);

        // Assert - Le repository ne doit être appelé qu'une seule fois
        verify(favoriteRepository, times(1)).findByUserIdAndContentType(userId, contentType);
    }

    @Test
    @DisplayName("Test de l'invalidation du cache lors de l'ajout d'un favori")
    void addFavorite_ShouldInvalidateCache() {
        // Arrange
        when(favoriteRepository.findByUserId(userId))
                .thenReturn(Arrays.asList(testFavorite));
        when(favoriteRepository.existsByUserIdAndContentId(anyString(), anyString()))
                .thenReturn(false);
        when(favoriteRepository.save(any(Favorite.class)))
                .thenReturn(testFavorite);

        // Act - Remplir le cache
        favoriteService.getUserFavorites(userId);
        
        // Act - Ajouter un favori (doit invalider le cache)
        favoriteService.addFavorite(testRequest);
        
        // Act - Récupérer à nouveau les favoris
        favoriteService.getUserFavorites(userId);

        // Assert - Le repository doit être appelé deux fois
        verify(favoriteRepository, times(2)).findByUserId(userId);
    }

    @Test
    @DisplayName("Test de l'invalidation du cache lors de la suppression d'un favori")
    void removeFavorite_ShouldInvalidateCache() {
        // Arrange
        when(favoriteRepository.findByUserId(userId))
                .thenReturn(Arrays.asList(testFavorite));
        when(favoriteRepository.findByUserIdAndContentId(userId, contentId))
                .thenReturn(Optional.of(testFavorite));
        doNothing().when(favoriteRepository).deleteByUserIdAndContentId(userId, contentId);

        // Act - Remplir le cache
        favoriteService.getUserFavorites(userId);
        
        // Act - Supprimer un favori (doit invalider le cache)
        favoriteService.removeFavorite(userId, contentId);
        
        // Act - Récupérer à nouveau les favoris
        favoriteService.getUserFavorites(userId);

        // Assert - Le repository doit être appelé deux fois
        verify(favoriteRepository, times(2)).findByUserId(userId);
    }

    @Test
    @DisplayName("Test de l'invalidation du cache lors du toggle d'un favori")
    void toggleFavorite_ShouldInvalidateCache() {
        // Arrange
        when(favoriteRepository.findByUserId(userId))
                .thenReturn(Arrays.asList(testFavorite));
        when(favoriteRepository.findByUserIdAndContentId(userId, contentId))
                .thenReturn(Optional.of(testFavorite));
        doNothing().when(favoriteRepository).deleteByUserIdAndContentId(userId, contentId);

        // Act - Remplir le cache
        favoriteService.getUserFavorites(userId);
        
        // Act - Toggle un favori (doit invalider le cache)
        favoriteService.toggleFavorite(testRequest);
        
        // Act - Récupérer à nouveau les favoris
        favoriteService.getUserFavorites(userId);

        // Assert - Le repository doit être appelé deux fois
        verify(favoriteRepository, times(2)).findByUserId(userId);
    }
}