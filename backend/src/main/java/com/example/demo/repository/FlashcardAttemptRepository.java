package com.example.demo.repository;

import com.example.demo.model.entity.flashcard.FlashcardAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlashcardAttemptRepository extends JpaRepository<FlashcardAttempt, Long> {

    List<FlashcardAttempt> findByUserIdAndFlashcard_FlashcardSet_Id(Long userId, Long flashcardFlashcardSetId);

    @Query("SELECT DISTINCT fa.user.id FROM FlashcardAttempt fa WHERE fa.attemptDate > :date")
    List<Long> findUserIdsAfter(@Param("date") LocalDateTime date);
    long countByUser_Id(Long userId);
}