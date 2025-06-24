package com.example.demo.repository;

import com.example.demo.model.entity.User;
import com.example.demo.model.entity.flashcard.Flashcard;
import com.example.demo.model.entity.flashcard.FlashcardAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashcardAttemptRepository extends JpaRepository<FlashcardAttempt, Long> {
    List<FlashcardAttempt> findByUserAndFlashcard(User user, Flashcard flashcard);

    List<FlashcardAttempt> findByUserIdAndFlashcard_FlashcardSet_Id(Long userId, Long flashcardFlashcardSetId);
}
