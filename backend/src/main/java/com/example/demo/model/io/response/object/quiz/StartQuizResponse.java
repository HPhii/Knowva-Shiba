package com.example.demo.model.io.response.object.quiz;

import com.example.demo.model.entity.quiz.QuizAttempt;
import com.example.demo.model.io.dto.QuizQuestionDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartQuizResponse {
    private QuizAttempt attempt;
    private List<QuizQuestionDTO> questions;
}
