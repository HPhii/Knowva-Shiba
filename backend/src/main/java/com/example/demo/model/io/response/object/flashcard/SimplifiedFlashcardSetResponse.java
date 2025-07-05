package com.example.demo.model.io.response.object.flashcard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimplifiedFlashcardSetResponse {
    private String title;
    private String description;
    private String sourceType;
    private String language;
    private String cardType;
    private String visibility;
    private String category;
    private List<SimplifiedFlashcardResponse> flashcards;
}
