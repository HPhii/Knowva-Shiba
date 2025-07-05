package com.example.demo.service.impl;

import com.example.demo.model.io.dto.dashboard.*;
import com.example.demo.repository.*;
import com.example.demo.service.intface.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DashboardService implements IDashboardService {
    private final AccountRepository accountRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final QuizSetRepository quizSetRepository;
    private final FlashcardAttemptRepository flashcardAttemptRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    @Override
    public OverviewStats getOverviewStats() {
        long totalUsers = accountRepository.count();
        long totalFlashcardSets = flashcardSetRepository.count();
        long totalQuizSets = quizSetRepository.count();
        long totalFlashcardAttempts = flashcardAttemptRepository.count();
        long totalQuizAttempts = quizAttemptRepository.count();

        return new OverviewStats(totalUsers, totalFlashcardSets, totalQuizSets, totalFlashcardAttempts, totalQuizAttempts);
    }

    @Override
    public UserStats getUserStats() {
        long totalUsers = accountRepository.count();
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        long newUsersLast7Days = accountRepository.countByCreatedAtAfter(sevenDaysAgo);

        Set<Long> activeUsers = new HashSet<>();
        activeUsers.addAll(flashcardAttemptRepository.findUserIdsAfter(sevenDaysAgo));
        activeUsers.addAll(quizAttemptRepository.findUserIdsAfter(sevenDaysAgo));
        long activeUsersLast7Days = activeUsers.size();

        return new UserStats(totalUsers, newUsersLast7Days, activeUsersLast7Days);
    }

    @Override
    public ContentStats getContentStats() {
        long totalFlashcardSets = flashcardSetRepository.count();
        long totalQuizSets = quizSetRepository.count();
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        long newFlashcardSetsLast7Days = flashcardSetRepository.countByCreatedAtAfter(sevenDaysAgo);
        long newQuizSetsLast7Days = quizSetRepository.countByCreatedAtAfter(sevenDaysAgo);

        return new ContentStats(totalFlashcardSets, totalQuizSets, newFlashcardSetsLast7Days, newQuizSetsLast7Days);
    }
}
