package com.formations.favoris.controller;

import com.formations.favoris.dto.AddFavoriteRequest;
import com.formations.favoris.dto.FavoriteDto;
import com.formations.favoris.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Favoris", description = "API de gestion des favoris")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Récupérer tous les favoris d'un utilisateur", 
               description = "Retourne la liste de tous les favoris pour un utilisateur donné")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Favoris récupérés avec succès",
                     content = @Content(schema = @Schema(implementation = FavoriteDto.class))),
        @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé"),
        @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    public ResponseEntity<List<FavoriteDto>> getUserFavorites(
            @Parameter(description = "ID de l'utilisateur", required = true)
            @PathVariable String userId) {
        log.info("Récupération des favoris pour l'utilisateur: {}", userId);
        List<FavoriteDto> favorites = favoriteService.getUserFavorites(userId);
        return ResponseEntity.ok(favorites);
    }

    @GetMapping("/user/{userId}/type/{contentType}")
    @Operation(summary = "Récupérer les favoris par type", 
               description = "Retourne la liste des favoris d'un utilisateur filtrés par type de contenu")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Favoris récupérés avec succès",
                     content = @Content(schema = @Schema(implementation = FavoriteDto.class))),
        @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé"),
        @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    public ResponseEntity<List<FavoriteDto>> getUserFavoritesByType(
            @Parameter(description = "ID de l'utilisateur", required = true)
            @PathVariable String userId,
            @Parameter(description = "Type de contenu (ex: COURSE, VIDEO, ARTICLE)", required = true)
            @PathVariable String contentType) {
        log.info("Récupération des favoris de type {} pour l'utilisateur: {}", contentType, userId);
        // Nous devons implémenter cette méthode dans le service
        List<FavoriteDto> favorites = favoriteService.getUserFavoritesByType(userId, contentType);
        return ResponseEntity.ok(favorites);
    }

    @PostMapping
    @Operation(summary = "Ajouter un favori", 
               description = "Ajoute un nouveau favori pour un utilisateur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Favori ajouté avec succès",
                     content = @Content(schema = @Schema(implementation = FavoriteDto.class))),
        @ApiResponse(responseCode = "400", description = "Requête invalide ou favori déjà existant"),
        @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    public ResponseEntity<FavoriteDto> addFavorite(
            @Parameter(description = "Données du favori à ajouter", required = true)
            @Valid @RequestBody AddFavoriteRequest request) {
        log.info("Ajout d'un favori pour l'utilisateur: {}", request.getUserId());
        try {
            FavoriteDto favoriteDto = favoriteService.addFavorite(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(favoriteDto);
        } catch (IllegalStateException e) {
            log.warn("Erreur lors de l'ajout du favori: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/user/{userId}/content/{contentId}")
    @Operation(summary = "Supprimer un favori", 
               description = "Supprime un favori pour un utilisateur et un contenu donnés")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Favori supprimé avec succès"),
        @ApiResponse(responseCode = "404", description = "Favori non trouvé"),
        @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    public ResponseEntity<Void> removeFavorite(
            @Parameter(description = "ID de l'utilisateur", required = true)
            @PathVariable String userId,
            @Parameter(description = "ID du contenu", required = true)
            @PathVariable String contentId) {
        log.info("Suppression d'un favori pour l'utilisateur: {} et le contenu: {}", userId, contentId);
        boolean removed = favoriteService.removeFavorite(userId, contentId);
        if (removed) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/toggle")
    @Operation(summary = "Basculer l'état d'un favori", 
               description = "Ajoute un favori s'il n'existe pas, le supprime s'il existe déjà")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Favori ajouté ou supprimé avec succès",
                     content = @Content(schema = @Schema(implementation = FavoriteDto.class))),
        @ApiResponse(responseCode = "400", description = "Requête invalide"),
        @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    public ResponseEntity<FavoriteDto> toggleFavorite(
            @Parameter(description = "Données du favori à basculer", required = true)
            @Valid @RequestBody AddFavoriteRequest request) {
        log.info("Bascule de l'état d'un favori pour l'utilisateur: {}", request.getUserId());
        FavoriteDto favoriteDto = favoriteService.toggleFavorite(request);
        if (favoriteDto != null) {
            return ResponseEntity.ok(favoriteDto);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
}