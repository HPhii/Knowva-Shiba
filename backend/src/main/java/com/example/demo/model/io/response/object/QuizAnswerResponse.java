package com.example.demo.model.io.response.object;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAnswerResponse {
    private Long id;
    private String answerText;
    private Boolean isCorrect;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
