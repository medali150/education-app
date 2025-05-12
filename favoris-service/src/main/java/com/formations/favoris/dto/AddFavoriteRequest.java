package com.formations.favoris.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddFavoriteRequest {
    
    @NotBlank(message = "L'ID de l'utilisateur est obligatoire")
    private String userId;
    
    @NotBlank(message = "L'ID du contenu est obligatoire")
    private String contentId;
    
    @NotBlank(message = "Le type de contenu est obligatoire")
    private String contentType;
    
    @NotBlank(message = "Le titre est obligatoire")
    private String title;
    
    private String description;
    private String thumbnailUrl;
}