package com.example.demo.controller;

import com.example.demo.model.entity.flashcard.Flashcard;
import com.example.demo.model.io.dto.SpacedRepetitionModeData;
import com.example.demo.model.io.dto.StudyProgressStats;
import com.example.demo.service.SpacedRepetitionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spaced-repetition")
@CrossOrigin("*")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class SpacedRepetitionController {

    private final SpacedRepetitionService spacedRepetitionService;

    @GetMapping("/mode-data")
    public ResponseEntity<SpacedRepetitionModeData> getSpacedRepetitionModeData(
            @RequestParam Long userId,
            @RequestParam Long flashcardSetId) {
        SpacedRepetitionModeData modeData = spacedRepetitionService.getModeData(userId, flashcardSetId);
        return ResponseEntity.ok(modeData);
    }

    @PostMapping("/set-new-flashcards-per-day")
    public ResponseEntity<Void> setNewFlashcardsPerDay(
            @RequestParam Long userId,
            @RequestParam Integer newFlashcardsPerDay) {
        spacedRepetitionService.setNewFlashcardsPerDay(userId, newFlashcardsPerDay);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/start-session")
    public ResponseEntity<List<Flashcard>> startStudySession(
            @RequestParam Long userId,
            @RequestParam Long flashcardSetId) {
        List<Flashcard> flashcards = spacedRepetitionService.startStudySession(userId, flashcardSetId);
        return ResponseEntity.ok(flashcards);
    }

    @PostMapping("/submit-review")
    public ResponseEntity<StudyProgressStats> submitReview(
            @RequestParam Long userId,
            @RequestParam Long flashcardId,
            @RequestParam Long flashcardSetId,
            @RequestParam Boolean knowsCard) {
        StudyProgressStats stats = spacedRepetitionService.submitReview(userId, flashcardId, flashcardSetId, knowsCard);
        return ResponseEntity.ok(stats);
    }
}