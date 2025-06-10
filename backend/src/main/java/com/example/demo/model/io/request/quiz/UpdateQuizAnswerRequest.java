package com.example.demo.model.io.request.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuizAnswerRequest {
    private Long id;
    private String answerText;
    private Boolean isCorrect;
}
