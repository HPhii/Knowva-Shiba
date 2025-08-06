package com.example.demo.model.io.response.object;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatsResponse {
    private long totalFlashcardSets;
    private long totalQuizSets;
    private long totalFlashcardAttempts;
    private long totalQuizAttempts;
    private Double averageQuizScore;
}