package com.example.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
        String url = "http://" + flaskHost + "/exam-mode-grade";

        return restTemplate.postForObject(url, requestEntity, String.class);
    }
}
