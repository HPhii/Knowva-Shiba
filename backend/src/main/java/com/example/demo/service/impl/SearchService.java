package com.example.demo.service.impl;

import com.example.demo.mapper.AccountMapper;
import com.example.demo.mapper.FlashcardSetManualMapper;
import com.example.demo.mapper.QuizSetManualMapper;
import com.example.demo.model.entity.Account;
import com.example.demo.model.entity.User;
import com.example.demo.model.entity.flashcard.FlashcardAccessControl;
import com.example.demo.model.entity.flashcard.FlashcardSet;
import com.example.demo.model.entity.quiz.QuizAccessControl;
import com.example.demo.model.entity.quiz.QuizSet;
import com.example.demo.model.enums.Visibility;
import com.example.demo.model.io.response.object.AccountResponse;
import com.example.demo.model.io.response.paged.PagedAccountResponse;
import com.example.demo.model.io.response.paged.PagedFlashcardSetResponse;
import com.example.demo.model.io.response.paged.PagedQuizSetResponse;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.FlashcardAccessControlRepository;
import com.example.demo.repository.FlashcardSetRepository;
import com.example.demo.repository.QuizAccessControlRepository;
import com.example.demo.repository.QuizSetRepository;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.ISearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService implements ISearchService {
    private final FlashcardSetRepository flashcardSetRepository;
    private final QuizSetRepository quizSetRepository;
    private final QuizSetManualMapper quizSetManualMapper;
    private final FlashcardSetManualMapper flashcardSetManualMapper;
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final IAccountService accountService;
    private final FlashcardAccessControlRepository flashcardAccessControlRepository;
    private final QuizAccessControlRepository quizAccessControlRepository;

    @Override
    public PagedQuizSetResponse searchQuizSets(String keyword, Pageable pageable) {
        Page<QuizSet> pageQuizSet = quizSetRepository.searchQuizSets(keyword, pageable);
        User currentUser = accountService.getCurrentAccount().getUser();
        
        // Filter quiz sets based on permissions
        List<QuizSet> filteredQuizSets = pageQuizSet.getContent().stream()
                .filter(quizSet -> hasQuizSetAccess(quizSet, currentUser))
                .collect(Collectors.toList());
        
        // Create a new page with the filtered results
        Page<QuizSet> filteredPage = new PageImpl<>(
                filteredQuizSets, 
                pageable,
                filteredQuizSets.size() // This is not ideal for pagination, but works for filtering results
        );

        return new PagedQuizSetResponse(
                quizSetManualMapper.mapToQuizSetResponseList(filteredQuizSets),
                filteredPage.getTotalElements(),
                filteredPage.getTotalPages(),
                pageable.getPageNumber()
        );
    }

    @Override
    public PagedFlashcardSetResponse searchFlashcardSets(String keyword, Pageable pageable) {
        Page<FlashcardSet> pageFlashcardSet = flashcardSetRepository.searchFlashcardSets(keyword, pageable);
        User currentUser = accountService.getCurrentAccount().getUser();
        
        // Filter flashcard sets based on permissions
        List<FlashcardSet> filteredFlashcardSets = pageFlashcardSet.getContent().stream()
                .filter(flashcardSet -> hasFlashcardSetAccess(flashcardSet, currentUser))
                .collect(Collectors.toList());
        
        // Create a new page with the filtered results
        Page<FlashcardSet> filteredPage = new PageImpl<>(
                filteredFlashcardSets, 
                pageable, 
                filteredFlashcardSets.size() // This is not ideal for pagination, but works for filtering results
        );

        return new PagedFlashcardSetResponse(
                flashcardSetManualMapper.mapToFlashcardSetResponseList(filteredFlashcardSets),
                filteredPage.getTotalElements(),
                filteredPage.getTotalPages(),
                pageable.getPageNumber()
        );
    }

    @Override
    public PagedAccountResponse searchAccounts(String keyword, Pageable pageable) {
        if (!keyword.endsWith("*")) {
            keyword += "*";
        }
        Page<Account> accountPage = accountRepository.searchAccounts(keyword, pageable);
        List<AccountResponse> accountResponses = accountPage.getContent().stream()
                .map(accountMapper::toAccountResponse)
                .collect(Collectors.toList());
        return new PagedAccountResponse(
                accountResponses,
                accountPage.getTotalElements(),
                accountPage.getTotalPages(),
                accountPage.getNumber()
        );
    }
    
    /**
     * Check if the current user has access to view the flashcard set
     */
    private boolean hasFlashcardSetAccess(FlashcardSet flashcardSet, User currentUser) {
        // Owner has access
        if (flashcardSet.getOwner().getId().equals(currentUser.getId())) {
            return true;
        }
        
        // Public sets are visible to everyone
        if (flashcardSet.getVisibility() == Visibility.PUBLIC) {
            return true;
        }
        
        // For private sets, check if user has been given access
        if (flashcardSet.getVisibility() == Visibility.PRIVATE) {
            Optional<FlashcardAccessControl> access = flashcardAccessControlRepository
                    .findByFlashcardSetAndInvitedUser(flashcardSet, currentUser);
            return access.isPresent();
        }
        
        // Hidden sets are not visible in search results unless the user is the owner
        return false;
    }
    
    /**
     * Check if the current user has access to view the quiz set
     */
    private boolean hasQuizSetAccess(QuizSet quizSet, User currentUser) {
        if (quizSet.getOwner().getId().equals(currentUser.getId())) {
            return true;
        }

        if (quizSet.getVisibility() == Visibility.PUBLIC) {
            return true;
        }
        
        // For private sets, check if user has been given access
        if (quizSet.getVisibility() == Visibility.PRIVATE) {
            Optional<QuizAccessControl> access = quizAccessControlRepository
                    .findByQuizSetAndInvitedUser(quizSet, currentUser);
            return access.isPresent();
        }
        
        // Hidden sets are not visible in search results unless the user is the owner
        return false;
    }
}
