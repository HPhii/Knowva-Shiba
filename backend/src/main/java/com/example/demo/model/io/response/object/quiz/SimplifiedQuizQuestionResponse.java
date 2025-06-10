package com.example.demo.model.io.response.object.quiz;

import java.util.List;

public record SimplifiedQuizQuestionResponse(
        String questionText,
        String questionHtml,
        String imageUrl,
        Integer timeLimit,
        Integer order,
        List<SimplifiedQuizAnswerResponse> answers
) {}