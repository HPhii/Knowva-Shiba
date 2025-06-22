package com.example.demo.repository;

import com.example.demo.model.entity.flashcard.Flashcard;
import com.example.demo.model.entity.flashcard.FlashcardSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    List<Flashcard> findByFlashcardSet(FlashcardSet flashcardSet);
}
