package com.example.demo.service.template;

import com.example.demo.model.entity.flashcard.FlashcardSet;
import com.example.demo.model.entity.quiz.QuizAnswer;
import com.example.demo.model.entity.quiz.QuizQuestion;
import com.example.demo.model.io.response.object.flashcard.SimplifiedFlashcardResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizSetAIService extends AIServiceTemplate<List<QuizQuestion>> {

    public QuizSetAIService(RestTemplate restTemplate, ObjectMapper objectMapper, @Value("${flask.service.url}") String flaskHost) {
        super(restTemplate, objectMapper, flaskHost);
    }

    @Override
    protected String callAIService(Object input, String language, String type, Integer maxItems) {
        if (input instanceof FlashcardSet flashcardSet) {
            return callAIServiceForFlashcardSet(flashcardSet, language, type, maxItems);
        } else {
            return callAIServiceForGeneralInput(input, language, type, maxItems);
        }
    }

    private String callAIServiceForFlashcardSet(FlashcardSet flashcardSet, String language, String type, Integer maxItems) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<SimplifiedFlashcardResponse> simplifiedFlashcards = flashcardSet.getFlashcards().stream()
                .map(f -> new SimplifiedFlashcardResponse(f.getFront(), f.getBack(), f.getImageUrl(), f.getOrder()))
                .collect(Collectors.toList());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.set("flashcards", objectMapper.valueToTree(simplifiedFlashcards));

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);
        String url = String.format("%s/generate-quiz-from-flashcards?language=%s&questionType=%s&maxQuestions=%d",
                flaskHost, language, type, maxItems != null ? maxItems : 5);

        return restTemplate.postForObject(url, requestEntity, String.class);
    }

    private String callAIServiceForGeneralInput(Object input, String language, String type, Integer maxItems) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            if (input instanceof String textInput && !textInput.isBlank()) {
                body.add("text", textInput);
            } else if (input instanceof List<?> files && !files.isEmpty()) {
                @SuppressWarnings("unchecked")
                List<MultipartFile> multipartFiles = (List<MultipartFile>) files;
                for (MultipartFile file : multipartFiles) {
                    body.add("files", new ByteArrayResource(file.getBytes()) {
                        @Override
                        public String getFilename() {
                            return file.getOriginalFilename();
                        }
                    });
                }
            } else {
                throw new IllegalArgumentException("Invalid input type for quiz generation.");
            }

            String url = String.format("%s/generate-quiz?language=%s&sourceType=%s&maxQuestions=%d",
                    flaskHost, language, type, maxItems != null ? maxItems : 5);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            return restTemplate.postForObject(url, requestEntity, String.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to call AI service for quiz", e);
        }
    }

    @Override
    protected List<QuizQuestion> parseAIResponse(String aiResponse, Integer maxItems) {
        try {
            JsonNode rootNode = objectMapper.readTree(aiResponse);
            JsonNode questionsNode = rootNode.get("questions");
            if (questionsNode == null || !questionsNode.isArray()) {
                throw new RuntimeException("Invalid AI response: 'questions' field is missing or not an array.");
            }

            List<QuizQuestion> questions = new ArrayList<>();
            int limit = maxItems != null ? Math.min(maxItems, questionsNode.size()) : questionsNode.size();

            for (int i = 0; i < limit; i++) {
                JsonNode qNode = questionsNode.get(i);
                if (qNode == null || !qNode.has("questionText") || !qNode.has("answers")) {
                    continue;
                }

                QuizQuestion question = QuizQuestion.builder()
                        .questionText(qNode.get("questionText").asText())
                        .questionHtml(qNode.has("questionHtml") ? qNode.get("questionHtml").asText(null) : null)
                        .imageUrl(qNode.has("imageUrl") ? qNode.get("imageUrl").asText(null) : null)
                        .timeLimit(qNode.has("timeLimit") ? qNode.get("timeLimit").asInt() : null)
                        .order(i + 1)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .answers(new ArrayList<>())
                        .build();

                JsonNode answersNode = qNode.get("answers");
                if (answersNode.isArray()) {
                    for (JsonNode aNode : answersNode) {
                        if (aNode.has("answerText") && aNode.has("isCorrect")) {
                            QuizAnswer answer = QuizAnswer.builder()
                                    .answerText(aNode.get("answerText").asText())
                                    .isCorrect(aNode.get("isCorrect").asBoolean())
                                    .createdAt(LocalDateTime.now())
                                    .updatedAt(LocalDateTime.now())
                                    .build();
                            question.getAnswers().add(answer);
                        }
                    }
                }
                questions.add(question);
            }
            return questions;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse AI response for quiz", e);
        }
    }
}
