package com.example.demo.mapper;

import com.example.demo.model.entity.flashcard.Flashcard;
import com.example.demo.model.entity.flashcard.FlashcardSet;
import com.example.demo.model.io.response.object.flashcard.FlashcardResponse;
import com.example.demo.model.io.response.object.flashcard.FlashcardSetResponse;
import com.example.demo.model.io.response.object.flashcard.SimplifiedFlashcardResponse;
import com.example.demo.model.io.response.object.flashcard.SimplifiedFlashcardSetResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FlashcardSetManualMapper {
    public FlashcardSetResponse mapToFlashcardSetResponse(FlashcardSet flashcardSet) {
        if (flashcardSet == null) return null;

        List<FlashcardResponse> flashcardResponses = flashcardSet.getFlashcards() != null
                ? flashcardSet.getFlashcards().stream()
                .map(this::toFlashcardResponse)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return new FlashcardSetResponse(
                flashcardSet.getId(),
                flashcardSet.getOwner().getId(),
                flashcardSet.getTitle(),
                flashcardSet.getSourceType(),
                flashcardSet.getLanguage(),
                flashcardSet.getCardType(),
                flashcardSet.getVisibility(),
                flashcardSet.getCreatedAt(),
                flashcardSet.getUpdatedAt(),
                flashcardResponses
        );
    }

    public FlashcardResponse toFlashcardResponse(Flashcard flashcard) {
        if (flashcard == null) return null;

        return new FlashcardResponse(
                flashcard.getId(),
                flashcard.getFront(),
                flashcard.getBack(),
                flashcard.getImageUrl(),
                flashcard.getOrder()
        );
    }

    public SimplifiedFlashcardSetResponse mapToSimplifiedFlashcardSetResponse(FlashcardSet flashcardSet) {
        if (flashcardSet == null) return null;

        List<SimplifiedFlashcardResponse> flashcards = flashcardSet.getFlashcards() != null
                ? flashcardSet.getFlashcards().stream()
                .map(this::toSimplifiedFlashcardResponse)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return new SimplifiedFlashcardSetResponse(
                flashcardSet.getTitle(),
                flashcardSet.getSourceType().name(),
                flashcardSet.getLanguage(),
                flashcardSet.getCardType().name(),
                flashcardSet.getVisibility().name(),
                flashcards
        );
    }

    public SimplifiedFlashcardResponse toSimplifiedFlashcardResponse(Flashcard flashcard) {
        return new SimplifiedFlashcardResponse(
                flashcard.getFront(),
                flashcard.getBack(),
                flashcard.getImageUrl(),
                flashcard.getOrder()
        );
    }

    public List<FlashcardSetResponse> mapToFlashcardSetResponseList(List<FlashcardSet> flashcardSets) {
        if (flashcardSets == null) return new ArrayList<>();
        return flashcardSets.stream()
                .map(this::mapToFlashcardSetResponse)
                .collect(Collectors.toList());
    }
}
