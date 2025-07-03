package com.example.demo.repository;

import com.example.demo.model.entity.flashcard.FlashcardSet;
import com.example.demo.model.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlashcardSetRepository extends JpaRepository<FlashcardSet, Long> {
    List<FlashcardSet> findAllByOwner_Id(Long ownerId);

    List<FlashcardSet> findAllByCategory(Category category);

//    long countByCreatedAtAfter(LocalDateTime date);

    long countByCreatedAtAfter(LocalDateTime createdAtAfter);

    @Query("SELECT fs.sourceType, COUNT(fs.id) FROM FlashcardSet fs GROUP BY fs.sourceType")
    List<Object[]> findCountBySourceType();

    @Query("SELECT fs.language, COUNT(fs.id) FROM FlashcardSet fs GROUP BY fs.language")
    List<Object[]> findCountByLanguage();

    @Query("SELECT fs.category, COUNT(fs.id) FROM FlashcardSet fs GROUP BY fs.category")
    List<Object[]> findCountByCategory();
}
