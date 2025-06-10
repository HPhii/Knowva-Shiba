package com.example.demo.model.io.response.object.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAnswerResponse {
    private Long id;
    private String answerText;
    private Boolean isCorrect;
}
