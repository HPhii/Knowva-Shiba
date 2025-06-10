package com.example.demo.model.io.request.quiz;

import com.example.demo.model.enums.QuestionType;
import com.example.demo.model.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuizSetRequest {
    private String title;
    private String language;
    private QuestionType questionType;
    private Integer maxQuestions;
    private Visibility visibility;
    private Integer timeLimit;
    private List<UpdateQuizQuestionRequest> questions;
}
