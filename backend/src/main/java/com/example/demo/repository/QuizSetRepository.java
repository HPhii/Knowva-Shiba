package com.example.demo.repository;

import com.example.demo.model.entity.quiz.QuizSet;
import com.example.demo.model.enums.Category;
import com.example.demo.model.enums.SourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizSetRepository extends JpaRepository<QuizSet, Long> {
    Optional<QuizSet> findByIdAndSourceType(Long id, SourceType sourceType);
    List<QuizSet> findAllByOwner_Id(Long userId);
    // find all by category
    List<QuizSet> findAllByCategory(Category category);

    long countByCreatedAtAfter(LocalDateTime createdAtAfter);
}
