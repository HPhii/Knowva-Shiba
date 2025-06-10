package com.example.demo.model.io.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionReview {
    private Long questionId;
    private Long userAnswerId;
    private Long correctAnswerId;
    private boolean correct;
    private List<QuizAnswerDTO> answers;
}
