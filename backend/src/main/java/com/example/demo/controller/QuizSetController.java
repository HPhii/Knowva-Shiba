package com.example.demo.controller;

import com.example.demo.model.io.request.quiz.CreateQuizSetRequest;
import com.example.demo.model.io.request.quiz.SaveQuizSetRequest;
import com.example.demo.model.io.request.quiz.UpdateQuizSetRequest;
import com.example.demo.model.io.response.object.quiz.QuizSetResponse;
import com.example.demo.model.io.response.object.quiz.SimplifiedQuizSetResponse;
import com.example.demo.service.intface.IQuizSetService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "text", required = false) String inputText) {
        SimplifiedQuizSetResponse response = quizSetService.generateQuizSet(quizSetRequest, files, inputText);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/save")
    public ResponseEntity<QuizSetResponse> saveQuizSet(@RequestBody SaveQuizSetRequest request) {
        QuizSetResponse response = quizSetService.saveQuizSet(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<QuizSetResponse>> getQuizSetsOfUser(@PathVariable Long userId) {
        List<QuizSetResponse> responses = quizSetService.getQuizSetsOfUser(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/all")
    public ResponseEntity<List<QuizSetResponse>> getAllQuizSets() {
        List<QuizSetResponse> responses = quizSetService.getAllQuizSets();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizSetResponse> getQuizSetById(@PathVariable Long id) {
        QuizSetResponse response = quizSetService.getQuizSetById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<QuizSetResponse> deleteQuizSetById(@PathVariable Long id) {
        QuizSetResponse response = quizSetService.deleteQuizSetById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{quizSetId}")
    public ResponseEntity<QuizSetResponse> updateQuizSet(
            @PathVariable Long quizSetId,
            @RequestBody UpdateQuizSetRequest request) {
        QuizSetResponse response = quizSetService.updateQuizSet(quizSetId, request);
        return ResponseEntity.ok(response);
    }
}
