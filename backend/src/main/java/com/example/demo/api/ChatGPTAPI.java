package com.example.demo.api;

import com.example.demo.model.io.request.PromptRequest;
import com.example.demo.service.intface.IChatGPTService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
public class ChatGPTAPI {
    private final IChatGPTService chatGPTService;

    @PostMapping("/chat")
    public String chat(@RequestBody PromptRequest promptRequest){
        return chatGPTService.getResponse(promptRequest);
    }
}
