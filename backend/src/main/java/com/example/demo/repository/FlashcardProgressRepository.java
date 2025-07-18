package com.example.demo.repository;

import com.example.demo.model.entity.User;
import com.example.demo.model.entity.flashcard.Flashcard;
import com.example.demo.model.entity.flashcard.FlashcardProgress;
import com.example.demo.model.entity.flashcard.FlashcardSet;
import com.example.demo.model.enums.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardProgressRepository extends JpaRepository<FlashcardProgress, Long> {
    List<FlashcardProgress>  findByUserAndFlashcard_FlashcardSet(User user, FlashcardSet flashcardSet);

    Optional<FlashcardProgress> findByUserAndFlashcard(User user, Flashcard flashcard);

    Optional<List<FlashcardProgress>> findByUser_Id(Long userId);

    long countByUserAndFlashcard_FlashcardSetAndStatus(User user, FlashcardSet flashcardSet, CardStatus status);

    long countByUserAndFlashcard_FlashcardSetAndStatusAndNextDueDateBefore(
            User user, FlashcardSet flashcardFlashcardSet, CardStatus status, LocalDate nextDueDateBefore);

    List<FlashcardProgress> findByNextDueDate(LocalDate nextDueDate);
}
