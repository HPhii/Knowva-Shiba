package com.example.demo.model.io.response.paged;

import com.example.demo.model.io.response.object.flashcard.FlashcardSetResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagedFlashcardSetResponse {
    private List<FlashcardSetResponse> flashcardSets;
    private long totalElements;
    private int totalPages;
    private int currentPage;
}
