package com.example.demo.model.io.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EngagementStats {
    private List<TopUser> mostActiveUsers;
    private List<TopSet> mostAttemptedFlashcardSets;
    private List<TopSet> mostAttemptedQuizSets;
}
