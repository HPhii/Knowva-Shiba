package com.example.demo.repository;

import com.example.demo.model.entity.User;
import com.example.demo.model.entity.flashcard.FlashcardProgress;
import com.example.demo.model.entity.flashcard.FlashcardSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashcardProgressRepository extends JpaRepository<FlashcardProgress, Long> {
    List<FlashcardProgress>  findByUserAndFlashcard_FlashcardSet(User user, FlashcardSet flashcardSet);
}
