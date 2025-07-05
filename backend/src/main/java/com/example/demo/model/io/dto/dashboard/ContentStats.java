package com.example.demo.model.io.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentStats {
    private long totalFlashcardSets;
    private long totalQuizSets;
    private long newFlashcardSetsLast7Days;
    private long newQuizSetsLast7Days;
}