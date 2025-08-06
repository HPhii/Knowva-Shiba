package com.example.demo.repository;

import com.example.demo.model.entity.quiz.QuizSet;
import com.example.demo.model.enums.Category;
import com.example.demo.model.enums.SourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizSetRepository extends JpaRepository<QuizSet, Long> {
    Optional<QuizSet> findByIdAndSourceType(Long id, SourceType sourceType);
    List<QuizSet> findAllByOwner_Id(Long userId);
    List<QuizSet> findAllByCategory(Category category);
    long countByOwner_Id(Long ownerId);
    long countByCreatedAtAfter(LocalDateTime createdAtAfter);

    @Query(value = "SELECT * FROM quiz_sets WHERE MATCH(title, description) AGAINST (?1)",
            countQuery = "SELECT count(*) FROM quiz_sets WHERE MATCH(title, description) AGAINST (?1)",
            nativeQuery = true)
    Page<QuizSet> searchQuizSets(String keyword, Pageable pageable);
}