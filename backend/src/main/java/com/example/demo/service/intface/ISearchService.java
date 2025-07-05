package com.example.demo.service.intface;

import com.example.demo.model.io.response.paged.PagedAccountResponse;
import com.example.demo.model.io.response.paged.PagedFlashcardSetResponse;
import com.example.demo.model.io.response.paged.PagedQuizSetResponse;
import org.springframework.data.domain.Pageable;

public interface ISearchService {
    PagedQuizSetResponse searchQuizSets(String keyword, Pageable pageable);
    PagedFlashcardSetResponse searchFlashcardSets(String keyword, Pageable pageable);
    PagedAccountResponse searchAccounts(String keyword, Pageable pageable);
}
