package com.example.demo.controller;

import com.example.demo.model.io.response.paged.PagedAccountResponse;
import com.example.demo.model.io.response.paged.PagedFlashcardSetResponse;
import com.example.demo.model.io.response.paged.PagedQuizSetResponse;
import com.example.demo.service.intface.ISearchService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@CrossOrigin("*")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class SearchController {
    private final ISearchService searchService;

    @GetMapping("/quiz-sets")
    public ResponseEntity<PagedQuizSetResponse> searchQuizSets(@RequestParam String keyword,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "8") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedQuizSetResponse quizSets = searchService.searchQuizSets(keyword, pageable);
        return ResponseEntity.ok(quizSets);
    }

    @GetMapping("/flashcard-sets")
    public ResponseEntity<PagedFlashcardSetResponse> searchFlashcardSets(@RequestParam String keyword,
                                                                         @RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "8") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedFlashcardSetResponse flashcardSets = searchService.searchFlashcardSets(keyword, pageable);
        return ResponseEntity.ok(flashcardSets);
    }

    @GetMapping("/accounts")
    public ResponseEntity<PagedAccountResponse> searchAccounts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PagedAccountResponse accounts = searchService.searchAccounts(keyword, pageable);
        return ResponseEntity.ok(accounts);
    }
}
