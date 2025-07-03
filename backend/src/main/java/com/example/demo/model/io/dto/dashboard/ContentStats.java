package com.example.demo.model.io.dto.dashboard;

import com.example.demo.model.enums.Category;
import com.example.demo.model.enums.SourceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentStats {
    private long totalFlashcardSets;
    private long totalQuizSets;
    private long newFlashcardSetsLast7Days;
    private long newQuizSetsLast7Days;
    private List<TopSet> topFlashcardSets;
    private List<TopSet> topQuizSets;
    private Map<SourceType, Long> flashcardSetSourceTypeDistribution;
    private Map<String, Long> flashcardSetLanguageDistribution;
    private Map<Category, Long> flashcardSetCategoryDistribution;
}