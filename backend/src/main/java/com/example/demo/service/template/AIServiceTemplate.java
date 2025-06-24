package com.example.demo.service.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;


public abstract class AIServiceTemplate<T> {

    protected final RestTemplate restTemplate;
    protected final ObjectMapper objectMapper;
    protected final String flaskHost;

    public AIServiceTemplate(RestTemplate restTemplate, ObjectMapper objectMapper, String flaskHost) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.flaskHost = flaskHost;
    }

    public T generateFromAI(Object input, String language, String type, Integer maxItems) {
        String aiResponse = callAIService(input, language, type, maxItems);
        return parseAIResponse(aiResponse, maxItems);
    }

    protected abstract String callAIService(Object input, String language, String type, Integer maxItems);

    protected abstract T parseAIResponse(String aiResponse, Integer maxItems);
}
