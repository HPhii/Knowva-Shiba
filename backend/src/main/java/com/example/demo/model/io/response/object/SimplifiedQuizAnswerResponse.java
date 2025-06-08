package com.example.demo.model.io.response.object;

public record SimplifiedQuizAnswerResponse(
        String answerText,
        Boolean isCorrect
) {}
