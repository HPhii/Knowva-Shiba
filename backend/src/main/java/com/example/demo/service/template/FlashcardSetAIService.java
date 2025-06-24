package com.example.demo.service.template;

import com.example.demo.model.entity.flashcard.Flashcard;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.List;

@Service
public class FlashcardSetAIService extends AIServiceTemplate<List<Flashcard>> {

    public FlashcardSetAIService(RestTemplate restTemplate, ObjectMapper objectMapper, @Value("${flask.service.url}") String flaskHost) {
        super(restTemplate, objectMapper, flaskHost);
    }

    @Override
    protected String callAIService(Object input, String language, String type, Integer maxItems) {
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
                throw new IllegalArgumentException("Either files or text must be provided.");
            }

            String url = "http://" + flaskHost + "/generate-flashcards?language=" + language +
                    "&sourceType=" + type +
                    "&maxFlashcards=" + (maxItems != null ? maxItems : 5);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            return restTemplate.postForObject(url, requestEntity, String.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to call AI service for flashcards", e);
        }
    }

    @Override
    protected List<Flashcard> parseAIResponse(String aiResponse, Integer maxItems) {
        try {
            JsonNode rootNode = objectMapper.readTree(aiResponse);
            List<Flashcard> flashcards = new ArrayList<>();
            JsonNode flashcardsNode = rootNode.get("flashcards");

            int limit = maxItems != null ? Math.min(maxItems, flashcardsNode.size()) : flashcardsNode.size();
            for (int i = 0; i < limit; i++) {
                JsonNode fNode = flashcardsNode.get(i);
                Flashcard flashcard = Flashcard.builder()
                        .front(fNode.get("front").asText())
                        .back(fNode.get("back").asText())
                        .imageUrl(fNode.has("imageUrl") ? fNode.get("imageUrl").asText(null) : null)
                        .order(i + 1)
                        .build();
                flashcards.add(flashcard);
            }
            return flashcards;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse AI response for flashcards", e);
        }
    }
}
