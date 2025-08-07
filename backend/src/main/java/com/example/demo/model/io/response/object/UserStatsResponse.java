package com.example.demo.model.io.response.object;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {
    private long totalFlashcardSets;
    private long totalQuizSets;
    private long totalFlashcardAttempts;
    private long totalQuizAttempts;
    private Double averageQuizScore;
}