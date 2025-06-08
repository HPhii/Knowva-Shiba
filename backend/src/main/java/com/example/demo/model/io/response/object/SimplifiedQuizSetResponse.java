package com.example.demo.model.io.response.object;

import java.util.List;

public record SimplifiedQuizSetResponse(
        String title,
        String sourceType,
        String language,
        String questionType,
        int maxQuestions,
        String visibility,
        Integer timeLimit,
        List<SimplifiedQuizQuestionResponse> questions
) {}
