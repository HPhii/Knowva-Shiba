package com.example.demo.model.io.request.flashcard;

import com.example.demo.model.enums.CardType;
import com.example.demo.model.enums.SourceType;
import com.example.demo.model.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveFlashcardSetRequest {
    private String title;
    private SourceType sourceType;
    private String language;
    private CardType cardType;
    private Visibility visibility;
    private List<SaveFlashcardRequest> flashcards;
}
