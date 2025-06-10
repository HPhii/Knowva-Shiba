package com.example.demo.model.io.request.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveQuizQuestionRequest {
    private String questionText;
    private String questionHtml;
    private String imageUrl;
    private Integer timeLimit;
    private Integer order;
    private List<SaveQuizAnswerRequest> answers;
}
