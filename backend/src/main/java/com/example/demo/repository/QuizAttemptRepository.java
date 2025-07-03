package com.example.demo.repository;

import com.example.demo.model.entity.quiz.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    @Query("SELECT DISTINCT qa.user.id FROM QuizAttempt qa WHERE qa.startedAt > :date")
    List<Long> findUserIdsAfter(@Param("date") LocalDateTime date);

    @Query("SELECT qs.id, qs.title, COUNT(qa.id) " +
            "FROM QuizAttempt qa JOIN qa.quizSet qs " +
            "GROUP BY qs.id, qs.title ORDER BY COUNT(qa.id) DESC")
    List<Object[]> findTopQuizSets(@Param("limit") int limit);

    @Query("SELECT AVG(qa.score) FROM QuizAttempt qa")
    Double findAverageScore();
}
