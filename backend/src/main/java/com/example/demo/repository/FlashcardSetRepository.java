package com.example.demo.repository;

import com.example.demo.model.entity.flashcard.FlashcardSet;
import com.example.demo.model.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlashcardSetRepository extends JpaRepository<FlashcardSet, Long> {
    List<FlashcardSet> findAllByOwner_Id(Long ownerId);
    List<FlashcardSet> findAllByCategory(Category category);
    long countByOwner_Id(Long ownerId);
    long countByCreatedAtAfter(LocalDateTime createdAtAfter);

    @Query(value = "SELECT * FROM flashcard_sets WHERE MATCH(title, description) AGAINST (?1)",
            countQuery = "SELECT count(*) FROM flashcard_sets WHERE MATCH(title, description) AGAINST (?1)",
            nativeQuery = true)
    Page<FlashcardSet> searchFlashcardSets(String keyword, Pageable pageable);
}
