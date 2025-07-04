package com.example.demo.repository;

import com.example.demo.model.entity.User;
import com.example.demo.model.entity.flashcard.Flashcard;
import com.example.demo.model.entity.flashcard.FlashcardAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlashcardAttemptRepository extends JpaRepository<FlashcardAttempt, Long> {
    List<FlashcardAttempt> findByUserAndFlashcard(User user, Flashcard flashcard);

    List<FlashcardAttempt> findByUserIdAndFlashcard_FlashcardSet_Id(Long userId, Long flashcardFlashcardSetId);

    @Query("SELECT DISTINCT fa.user.id FROM FlashcardAttempt fa WHERE fa.attemptDate > :date")
    List<Long> findUserIdsAfter(@Param("date") LocalDateTime date);

    @Query("SELECT fs.id, fs.title, COUNT(fa.id) " +
            "FROM FlashcardAttempt fa JOIN fa.flashcard f JOIN f.flashcardSet fs " +
            "GROUP BY fs.id, fs.title ORDER BY COUNT(fa.id) DESC")
    List<Object[]> findTopFlashcardSets(@Param("limit") int limit);
    
    @Query("SELECT fa.user.id, fa.user.fullName, COUNT(fa.id) " +
           "FROM FlashcardAttempt fa " +
           "GROUP BY fa.user.id, fa.user.fullName " +
           "ORDER BY COUNT(fa.id) DESC " +
           "LIMIT :limit")
    List<Object[]> findTopUsersByAttempts(@Param("limit") int limit);
}