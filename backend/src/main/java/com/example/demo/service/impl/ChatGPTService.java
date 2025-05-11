package com.example.demo.service.impl;

import com.example.demo.model.io.request.ChatGPTRequest;
import com.example.demo.model.io.request.PromptRequest;
import com.example.demo.model.io.response.object.ChatGPTResponse;
import com.example.demo.service.intface.IChatGPTService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatGPTService implements IChatGPTService {
    private final RestClient restClient;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${openapi.api.model}")
    private String model;

    @Override
    public String getResponse(PromptRequest promptRequest){

        ChatGPTRequest chatGPTRequest = new ChatGPTRequest(
                model,
                List.of(new ChatGPTRequest.Message("user", promptRequest.prompt()))
        );

        ChatGPTResponse response = restClient.post()
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(chatGPTRequest)
                .retrieve()
                .body(ChatGPTResponse.class);

        return response.choices().get(0).message().content();

    }
}
