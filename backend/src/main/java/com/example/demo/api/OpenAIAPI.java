package com.example.demo.api;

import com.example.demo.model.immutableDTOs.Answer;
import com.example.demo.model.immutableDTOs.Question;
import com.example.demo.service.intface.IOpenAIService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class OpenAIAPI {
    private final IOpenAIService openAIService;

    @PostMapping("/openai")
    public Answer getAnswer(@RequestBody Question question) {
        return openAIService.getResult(question);
    }
}
