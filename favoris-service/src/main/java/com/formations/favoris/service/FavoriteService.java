package com.formations.favoris.service;

import com.formations.favoris.dto.AddFavoriteRequest;
import com.formations.favoris.dto.FavoriteDto;
import com.formations.favoris.model.Favorite;
import com.formations.favoris.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    
    /**
     * Récupère tous les favoris d'un utilisateur
     * 
     * @param userId ID de l'utilisateur
     * @return Liste des favoris de l'utilisateur sous forme de DTO
     */
    @Cacheable(value = "favorites", key = "#userId")
    public List<FavoriteDto> getUserFavorites(String userId) {
        log.info("Cache miss - Récupération des favoris pour l'utilisateur: {}", userId);
        
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        log.debug("Nombre de favoris trouvés: {}", favorites.size());
        
        return favorites.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les favoris d'un utilisateur par type de contenu
     * 
     * @param userId ID de l'utilisateur
     * @param contentType Type de contenu
     * @return Liste des favoris de l'utilisateur pour le type de contenu spécifié
     */
    @Cacheable(value = "favoritesByType", key = "#userId + '_' + #contentType")
    public List<FavoriteDto> getUserFavoritesByType(String userId, String contentType) {
        log.info("Cache miss - Récupération des favoris de type {} pour l'utilisateur: {}", contentType, userId);
        
        List<Favorite> favorites = favoriteRepository.findByUserIdAndContentType(userId, contentType);
        log.debug("Nombre de favoris trouvés: {}", favorites.size());
        
        return favorites.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Ajoute un nouveau favori avec vérification pour éviter les doublons
     * 
     * @param request Données du favori à ajouter
     * @return Le favori ajouté sous forme de DTO
     * @throws IllegalStateException si le favori existe déjà
     */
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "favorites", key = "#request.userId"),
        @CacheEvict(value = "favoritesByType", key = "#request.userId + '_' + #request.contentType")
    })
    public FavoriteDto addFavorite(AddFavoriteRequest request) {
        log.info("Tentative d'ajout d'un favori pour l'utilisateur: {} et le contenu: {}", 
                request.getUserId(), request.getContentId());
        
        // Vérification pour éviter les doublons
        if (favoriteRepository.existsByUserIdAndContentId(request.getUserId(), request.getContentId())) {
            log.warn("Le favori existe déjà pour l'utilisateur: {} et le contenu: {}", 
                    request.getUserId(), request.getContentId());
            throw new IllegalStateException("Ce contenu est déjà dans vos favoris");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        Favorite favorite = Favorite.builder()
                .userId(request.getUserId())
                .contentId(request.getContentId())
                .contentType(request.getContentType())
                .title(request.getTitle())
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        Favorite savedFavorite = favoriteRepository.save(favorite);
        log.info("Favori ajouté avec succès, ID: {}", savedFavorite.getId());
        
        return convertToDto(savedFavorite);
    }
    
    /**
     * Supprime un favori
     * 
     * @param userId ID de l'utilisateur
     * @param contentId ID du contenu
     * @return true si le favori a été supprimé, false s'il n'existait pas
     */
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "favorites", key = "#userId"),
        @CacheEvict(value = "favoritesByType", allEntries = true)
    })
    public boolean removeFavorite(String userId, String contentId) {
        log.info("Tentative de suppression d'un favori pour l'utilisateur: {} et le contenu: {}", 
                userId, contentId);
        
        Optional<Favorite> favoriteOpt = favoriteRepository.findByUserIdAndContentId(userId, contentId);
        
        if (favoriteOpt.isPresent()) {
            favoriteRepository.deleteByUserIdAndContentId(userId, contentId);
            log.info("Favori supprimé avec succès");
            return true;
        } else {
            log.warn("Tentative de suppression d'un favori inexistant pour l'utilisateur: {} et le contenu: {}", 
                    userId, contentId);
            return false;
        }
    }
    
    /**
     * Fonction de bascule (toggle) pour ajouter/supprimer un favori
     * 
     * @param request Données du favori à ajouter/supprimer
     * @return Le favori ajouté sous forme de DTO ou null si le favori a été supprimé
     */
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "favorites", key = "#request.userId"),
        @CacheEvict(value = "favoritesByType", key = "#request.userId + '_' + #request.contentType")
    })
    public FavoriteDto toggleFavorite(AddFavoriteRequest request) {
        log.info("Toggle favori pour l'utilisateur: {} et le contenu: {}", 
                request.getUserId(), request.getContentId());
        
        Optional<Favorite> existingFavorite = favoriteRepository.findByUserIdAndContentId(
                request.getUserId(), request.getContentId());
        
        if (existingFavorite.isPresent()) {
            // Si le favori existe, on le supprime
            favoriteRepository.deleteByUserIdAndContentId(request.getUserId(), request.getContentId());
            log.info("Favori supprimé lors du toggle");
            return null;
        } else {
            // Si le favori n'existe pas, on l'ajoute
            log.info("Ajout d'un favori lors du toggle");
            return addFavorite(request);
        }
    }
    
    /**
     * Convertit une entité Favorite en DTO
     */
    private FavoriteDto convertToDto(Favorite favorite) {
        return FavoriteDto.builder()
                .id(favorite.getId())
                .userId(favorite.getUserId())
                .contentId(favorite.getContentId())
                .contentType(favorite.getContentType())
                .title(favorite.getTitle())
                .description(favorite.getDescription())
                .thumbnailUrl(favorite.getThumbnailUrl())
                .createdAt(favorite.getCreatedAt())
                .updatedAt(favorite.getUpdatedAt())
                .build();
    }
}