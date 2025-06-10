package com.example.demo.model.io.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptDetailResponse {
    private Long attemptId;
    private Float score;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private List<QuestionReview> reviews;
}
