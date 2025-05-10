package com.example.demo.service.impl;

import com.example.demo.model.immutableDTOs.Answer;
import com.example.demo.model.immutableDTOs.Question;
import com.example.demo.service.intface.IOpenAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class OpenAIService implements IOpenAIService {
    private final ChatModel chatModel;

    @Override
    public Answer getResult(Question question) {
        Prompt prompt = new PromptTemplate(question.question()).create();

        ChatResponse response = chatModel.call(prompt);
        if (response != null && !response.getResults().isEmpty()) {
            return new Answer(response.getResults().get(0).getOutput().getText());
        }

        return new Answer("No response received!!");
    }

}
