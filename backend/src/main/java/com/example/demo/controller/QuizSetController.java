package com.example.demo.controller;

import com.example.demo.model.io.request.CreateQuizSetRequest;
import com.example.demo.model.io.request.SaveQuizSetRequest;
import com.example.demo.model.io.response.object.QuizSetResponse;
import com.example.demo.model.io.response.object.SimplifiedQuizSetResponse;
import com.example.demo.service.intface.IQuizSetService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/quiz-sets")
@CrossOrigin("*")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class QuizSetController {

    private final IQuizSetService quizSetService;

    @PostMapping(value = "/generate", consumes = {"multipart/form-data"})
    public ResponseEntity<SimplifiedQuizSetResponse> generateQuizSet(
            @RequestPart("quizSet") CreateQuizSetRequest quizSetRequest,
            @RequestPart("file") MultipartFile file) {
        SimplifiedQuizSetResponse response = quizSetService.generateQuizSet(quizSetRequest, file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/save")
    public ResponseEntity<QuizSetResponse> saveQuizSet(@RequestBody SaveQuizSetRequest request) {
        QuizSetResponse response = quizSetService.saveQuizSet(request);
        return ResponseEntity.ok(response);
    }
}
