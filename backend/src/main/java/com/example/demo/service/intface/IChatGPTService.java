package com.example.demo.service.intface;

import com.example.demo.model.io.request.PromptRequest;

public interface IChatGPTService {
    String getResponse(PromptRequest prompt);
}
