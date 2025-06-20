package com.example.demo.controller;

import com.example.demo.model.entity.flashcard.Flashcard;
import com.example.demo.model.entity.flashcard.FlashcardAttempt;
import com.example.demo.model.io.request.flashcard.CreateFlashcardSetRequest;
import com.example.demo.model.io.request.flashcard.SaveFlashcardSetRequest;
import com.example.demo.model.io.request.flashcard.UpdateFlashcardSetRequest;
import com.example.demo.model.io.response.object.flashcard.FlashcardSetResponse;
import com.example.demo.model.io.response.object.flashcard.SimplifiedFlashcardSetResponse;
import com.example.demo.model.io.response.object.quiz.SimplifiedQuizSetResponse;
import com.example.demo.service.intface.IFlashcardSetService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/flashcard-sets")
@CrossOrigin("*")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class FlashcardSetController {
    private final IFlashcardSetService flashcardSetService;

    @PostMapping(value = "/generate", consumes = {"multipart/form-data"})
    public ResponseEntity<SimplifiedFlashcardSetResponse> generateFlashcardSet(
            @RequestPart("flashcardSet") CreateFlashcardSetRequest flashcardSetRequest,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "text", required = false) String inputText) {
        SimplifiedFlashcardSetResponse response = flashcardSetService.generateFlashcardSet(flashcardSetRequest, file, inputText);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/save")
    public ResponseEntity<FlashcardSetResponse> saveFlashcardSet(@RequestBody SaveFlashcardSetRequest request) {
        FlashcardSetResponse response = flashcardSetService.saveFlashcardSet(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{flashcardSetId}")
    public ResponseEntity<FlashcardSetResponse> updateFlashcardSet(
            @PathVariable Long flashcardSetId,
            @RequestBody UpdateFlashcardSetRequest request) {
        FlashcardSetResponse response = flashcardSetService.updateFlashcardSet(flashcardSetId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlashcardSetResponse> getFlashcardSetById(@PathVariable Long id) {
        FlashcardSetResponse response = flashcardSetService.getFlashcardSetById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<FlashcardSetResponse> deleteFlashcardSetById(@PathVariable Long id) {
        FlashcardSetResponse response = flashcardSetService.deleteFlashcardSetById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public ResponseEntity<List<FlashcardSetResponse>> getFlashcardSetsOfUser() {
        List<FlashcardSetResponse> responses = flashcardSetService.getFlashcardSetsOfUser();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/all")
    public ResponseEntity<List<FlashcardSetResponse>> getAllFlashcardSets() {
        List<FlashcardSetResponse> responses = flashcardSetService.getAllFlashcardSets();
        return ResponseEntity.ok(responses);
    }

    // Exam Mode: Submit user answer
    @PostMapping("/{flashcardSetId}/exam-mode/{flashcardId}")
    public ResponseEntity<FlashcardAttempt> submitExamMode(
            @PathVariable Long flashcardSetId,
            @PathVariable Long flashcardId,
            @RequestBody String userAnswer) {
        FlashcardAttempt response = flashcardSetService.examModeSubmit(flashcardSetId, flashcardId, userAnswer);
        return ResponseEntity.ok(response);
    }

    // Space Repetition Mode
    @GetMapping("/{flashcardSetId}/space-repetition")
    public ResponseEntity<List<Flashcard>> spaceRepetitionMode(
            @PathVariable Long flashcardSetId,
            @RequestParam Integer dailyLimit) {
        List<Flashcard> response = flashcardSetService.spaceRepetitionMode(flashcardSetId, dailyLimit);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{flashcardSetId}/generate-quiz")
    public ResponseEntity<SimplifiedQuizSetResponse> generateQuizMode(
            @PathVariable Long flashcardSetId,
            @RequestParam(defaultValue = "en") String language,
            @RequestParam(defaultValue = "MULTIPLE_CHOICE") String questionType,
            @RequestParam(defaultValue = "5") int maxQuestions) {
        SimplifiedQuizSetResponse response = flashcardSetService.generateQuizMode(flashcardSetId, language, questionType, maxQuestions);
        return ResponseEntity.ok(response);
    }
}
