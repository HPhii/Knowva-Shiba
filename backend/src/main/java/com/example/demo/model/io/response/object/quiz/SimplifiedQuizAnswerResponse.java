package com.example.demo.model.io.response.object.quiz;

public record SimplifiedQuizAnswerResponse(
        String answerText,
        Boolean isCorrect
) {}
