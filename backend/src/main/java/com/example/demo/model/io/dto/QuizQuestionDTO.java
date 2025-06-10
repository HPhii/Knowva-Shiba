package com.example.demo.model.io.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionDTO {
    private Long id;
    private String questionText;
    private String questionHtml;
    private String imageUrl;
    private Integer timeLimit;
    private Integer order;
    private List<QuizAnswerDTO> answers;
}
