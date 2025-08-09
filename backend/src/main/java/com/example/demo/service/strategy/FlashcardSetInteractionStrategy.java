package com.example.demo.service.strategy;

import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.model.entity.Comment;
import com.example.demo.model.entity.Rating;
import com.example.demo.model.entity.flashcard.FlashcardSet;
import com.example.demo.repository.FlashcardSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FlashcardSetInteractionStrategy implements EntityInteractionStrategy {

    private final FlashcardSetRepository flashcardSetRepository;

    @Override
    public String getEntityType() {
        return "flashcardset";
    }

    @Override
    public void setEntityForRating(Rating rating, Long entityId) {
        FlashcardSet flashcardSet = findEntityById(entityId);
        rating.setFlashcardSet(flashcardSet);
    }

    @Override
    public void setEntityForComment(Comment comment, Long entityId) {
        FlashcardSet flashcardSet = findEntityById(entityId);
        comment.setFlashcardSet(flashcardSet);
    }

    @Override
    public FlashcardSet findEntityById(Long entityId) {
        return flashcardSetRepository.findById(entityId)
                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found with id: " + entityId));
    }
}
