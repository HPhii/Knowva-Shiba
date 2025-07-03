package com.example.demo.utils;

import com.example.demo.model.io.response.object.quiz.SimplifiedQuizAnswerResponse;
import com.example.demo.model.io.response.object.quiz.SimplifiedQuizQuestionResponse;
import com.example.demo.model.io.response.object.quiz.SimplifiedQuizSetResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonNode parseJson(String aiResponse) {
        try {
            return objectMapper.readTree(aiResponse);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }

    public static SimplifiedQuizSetResponse parseQuiz(String aiResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(aiResponse);
            JsonNode questionsNode = rootNode.get("questions");

            List<SimplifiedQuizQuestionResponse> questions = new ArrayList<>();
            for (JsonNode qNode : questionsNode) {
                List<SimplifiedQuizAnswerResponse> answers = new ArrayList<>();
                for (JsonNode aNode : qNode.get("answers")) {
                    answers.add(new SimplifiedQuizAnswerResponse(
                            aNode.get("answerText").asText(),
                            aNode.get("isCorrect").asBoolean()
                    ));
                }
                questions.add(new SimplifiedQuizQuestionResponse(
                        qNode.get("questionText").asText(),
                        qNode.has("questionHtml") ? qNode.get("questionHtml").asText(null) : null,
                        qNode.has("imageUrl") ? qNode.get("imageUrl").asText(null) : null,
                        qNode.has("timeLimit") ? qNode.get("timeLimit").asInt() : null,
                        questions.size() + 1,
                        answers
                ));
            }

            return new SimplifiedQuizSetResponse(
                    "Generated Quiz from Flashcards",
                    "FLASHCARD",
                    "en",
                    "MULTIPLE_CHOICE",
                    questions.size(),
                    "PUBLIC",
                    "OTHER",
                    30 * questions.size(),
                    questions
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }
}

