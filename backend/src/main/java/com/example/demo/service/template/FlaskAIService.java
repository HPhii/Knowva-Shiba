package com.example.demo.service.template;

import com.example.demo.model.entity.flashcard.FlashcardSet;
import com.example.demo.model.io.response.object.flashcard.SimplifiedFlashcardResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlaskAIService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${flask.service.url}")
    private String flaskHost;

    public String evaluateAnswer(String correctAnswer, String userAnswer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("correctAnswer", correctAnswer);
        body.put("userAnswer", userAnswer);

        HttpEntity<String> requestEntity = new HttpEntity<>(body.toString(), headers);
        String url = flaskHost + "/exam-mode-grade";

        return restTemplate.postForObject(url, requestEntity, String.class);
    }

    public String callFlaskAIForQuizGeneration(FlashcardSet flashcardSet, String language, String questionType, int maxQuestions) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<SimplifiedFlashcardResponse> simplifiedFlashcards = flashcardSet.getFlashcards().stream()
                .map(flashcard -> new SimplifiedFlashcardResponse(
                        flashcard.getFront(),
                        flashcard.getBack(),
                        flashcard.getImageUrl(),
                        flashcard.getOrder()
                ))
                .collect(Collectors.toList());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Ensure LocalDateTime support
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.set("flashcards", objectMapper.valueToTree(simplifiedFlashcards));

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);
        String url = String.format("%s/generate-quiz-from-flashcards?language=%s&questionType=%s&maxQuestions=%d",
                flaskHost, language, questionType, maxQuestions);
        return restTemplate.postForObject(url, requestEntity, String.class);
    }
}
