package com.example.demo.model.io.response.object.flashcard;

import com.example.demo.model.enums.CardType;
import com.example.demo.model.enums.Category;
import com.example.demo.model.enums.SourceType;
import com.example.demo.model.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardSetResponse {
    private Long id;
    private Long userId;
    private String username;
    private String title;
    private String description;
    private SourceType sourceType;
    private String language;
    private CardType cardType;
    private Visibility visibility;
    private Category category;
    private List<FlashcardResponse> flashcards;
    private String accessToken;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
