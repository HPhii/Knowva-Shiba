package com.example.demo.model.io.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveQuizAnswerRequest {
    private String answerText;
    private Boolean isCorrect;
}
