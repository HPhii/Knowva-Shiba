package com.example.demo.model.io.response.object;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionResponse {
    private Long id;
    private String questionText;
    private String questionHtml;
    private String imageUrl;
    private Integer timeLimit;
    private Integer order;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<QuizAnswerResponse> answers;
}
