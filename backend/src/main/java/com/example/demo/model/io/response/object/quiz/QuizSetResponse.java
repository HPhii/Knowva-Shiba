package com.example.demo.model.io.response.object.quiz;

import com.example.demo.model.enums.Category;
import com.example.demo.model.enums.QuestionType;
import com.example.demo.model.enums.SourceType;
import com.example.demo.model.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizSetResponse {
    private Long id;
    private Long userId;
    private String username;
    private String title;
    private String description;
    private SourceType sourceType;
    private String language;
    private QuestionType questionType;
    private Integer maxQuestions;
    private Visibility visibility;
    private Category category;
    private Integer timeLimit;
    private List<QuizQuestionResponse> questions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
