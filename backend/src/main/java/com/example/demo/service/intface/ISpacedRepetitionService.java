package com.example.demo.service.intface;

import com.example.demo.model.entity.flashcard.Flashcard;
import com.example.demo.model.entity.flashcard.FlashcardAttempt;
import com.example.demo.model.entity.flashcard.FlashcardProgress;
import com.example.demo.model.io.dto.PerformanceStats;
import com.example.demo.model.io.dto.SpacedRepetitionModeData;
import com.example.demo.model.io.dto.StudyProgressStats;

import java.util.List;

public interface ISpacedRepetitionService {
    SpacedRepetitionModeData getModeData(Long userId, Long flashcardSetId);
    void setNewFlashcardsPerDay(Long userId, Long flashcardSetId, Integer newFlashcardsPerDay);
    List<Flashcard> startStudySession(Long userId, Long flashcardSetId);
//    StudyProgressStats submitReview(Long userId, Long flashcardId, Long flashcardSetId, Boolean knowsCard);
//    void updateFlashcardProgress(FlashcardProgress progress, boolean knowsCard);
    StudyProgressStats submitReview(Long userId, Long flashcardId, Long flashcardSetId, int quality);
    void updateFlashcardProgress(FlashcardProgress progress, int quality);
    PerformanceStats getPerformanceStats(Long userId, Long flashcardSetId);
    List<FlashcardAttempt> getStudyHistory(Long userId, Long flashcardSetId);
}
