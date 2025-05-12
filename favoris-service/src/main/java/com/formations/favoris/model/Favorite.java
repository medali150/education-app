package com.formations.favoris.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "favorites")
@CompoundIndexes({
    @CompoundIndex(name = "user_content_idx", def = "{userId: 1, contentId: 1}", unique = true)
})
public class Favorite {
    
    @Id
    private String id;
    
    private String userId;
    private String contentId;
    private String contentType; // ex: "COURSE", "VIDEO", "ARTICLE"
    private String title;
    private String description;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}