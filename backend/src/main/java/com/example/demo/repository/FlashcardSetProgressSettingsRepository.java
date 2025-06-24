package com.example.demo.repository;

import com.example.demo.model.entity.User;
import com.example.demo.model.entity.flashcard.FlashcardSet;
import com.example.demo.model.entity.flashcard.FlashcardSetProgressSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlashcardSetProgressSettingsRepository extends JpaRepository<FlashcardSetProgressSettings, Long> {
    Optional<FlashcardSetProgressSettings> findByUserAndFlashcardSet(User user, FlashcardSet flashcardSet);
}
