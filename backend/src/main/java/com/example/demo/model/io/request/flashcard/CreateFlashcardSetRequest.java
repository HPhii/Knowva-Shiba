package com.example.demo.model.io.request.flashcard;

import com.example.demo.model.enums.CardType;
import com.example.demo.model.enums.Category;
import com.example.demo.model.enums.SourceType;
import com.example.demo.model.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFlashcardSetRequest {
    private String title;
    private String description;
    private SourceType sourceType;
    private String language;
    private CardType cardType;
    private Integer maxFlashcards;
    private Visibility visibility;
    private Category category;
}
