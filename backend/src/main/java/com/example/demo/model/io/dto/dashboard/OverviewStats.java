package com.example.demo.model.io.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverviewStats {
    private long totalUsers;
    private long totalFlashcardSets;
    private long totalQuizSets;
    private long totalFlashcardAttempts;
    private long totalQuizAttempts;
}
