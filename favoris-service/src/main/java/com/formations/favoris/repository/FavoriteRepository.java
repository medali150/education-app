package com.formations.favoris.repository;

import com.formations.favoris.model.Favorite;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends MongoRepository<Favorite, String> {
    
    /**
     * Trouve tous les favoris d'un utilisateur
     * @param userId ID de l'utilisateur
     * @return Liste des favoris de l'utilisateur
     */
    List<Favorite> findByUserId(String userId);
    
    /**
     * Trouve les favoris d'un utilisateur par type de contenu
     * @param userId ID de l'utilisateur
     * @param contentType Type de contenu (ex: "COURSE", "VIDEO", "ARTICLE")
     * @return Liste des favoris de l'utilisateur pour le type de contenu spécifié
     */
    List<Favorite> findByUserIdAndContentType(String userId, String contentType);
    
    /**
     * Vérifie si un contenu est déjà favori pour un utilisateur
     * @param userId ID de l'utilisateur
     * @param contentId ID du contenu
     * @return true si le contenu est déjà favori, false sinon
     */
    boolean existsByUserIdAndContentId(String userId, String contentId);
    
    /**
     * Trouve un favori par userId et contentId
     * @param userId ID de l'utilisateur
     * @param contentId ID du contenu
     * @return Le favori s'il existe
     */
    Optional<Favorite> findByUserIdAndContentId(String userId, String contentId);
    
    /**
     * Supprime un favori par userId et contentId
     * @param userId ID de l'utilisateur
     * @param contentId ID du contenu
     */
    void deleteByUserIdAndContentId(String userId, String contentId);
}