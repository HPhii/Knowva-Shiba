package com.example.demo.model.io.response.object.quiz;

import java.util.List;

public record SimplifiedQuizSetResponse(
        String title,
        String description,
        String sourceType,
        String language,
        String questionType,
        int maxQuestions,
        String visibility,
        String category,
        Integer timeLimit,
        List<SimplifiedQuizQuestionResponse> questions
) {}
