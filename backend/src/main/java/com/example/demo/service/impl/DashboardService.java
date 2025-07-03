package com.example.demo.service.impl;

import com.example.demo.model.enums.Category;
import com.example.demo.model.enums.SourceType;
import com.example.demo.model.io.dto.dashboard.*;
import com.example.demo.repository.*;
import com.example.demo.service.intface.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

        List<Object[]> topFlashcardResults = flashcardAttemptRepository.findTopFlashcardSets(5);
        List<TopSet> topFlashcardSets = topFlashcardResults.stream()
                .map(obj -> new TopSet((Long) obj[0], (String) obj[1], (Long) obj[2]))
                .collect(Collectors.toList());

        List<Object[]> topQuizResults = quizAttemptRepository.findTopQuizSets(5);
        List<TopSet> topQuizSets = topQuizResults.stream()
                .map(obj -> new TopSet((Long) obj[0], (String) obj[1], (Long) obj[2]))
                .collect(Collectors.toList());

        Map<SourceType, Long> flashcardSourceDist = flashcardSetRepository.findCountBySourceType()
                .stream().collect(Collectors.toMap(obj -> (SourceType) obj[0], obj -> (Long) obj[1]));
        Map<String, Long> flashcardLangDist = flashcardSetRepository.findCountByLanguage()
                .stream().collect(Collectors.toMap(obj -> (String) obj[0], obj -> (Long) obj[1]));
        Map<Category, Long> flashcardCatDist = flashcardSetRepository.findCountByCategory()
                .stream().collect(Collectors.toMap(obj -> (Category) obj[0], obj -> (Long) obj[1]));

        return new ContentStats(totalFlashcardSets, totalQuizSets, newFlashcardSetsLast7Days, newQuizSetsLast7Days,
                topFlashcardSets, topQuizSets, flashcardSourceDist, flashcardLangDist, flashcardCatDist);
    }

    @Override
    public PerformanceStats getPerformanceStats() {
        Double averageQuizScore = quizAttemptRepository.findAverageScore();
        return new PerformanceStats(averageQuizScore != null ? averageQuizScore : 0.0);
    }

    @Override
    public EngagementStats getEngagementStats() {
        List<Object[]> topUserResults = flashcardAttemptRepository.findTopUsersByAttempts(5);
        List<TopUser> mostActiveUsers = topUserResults.stream()
                .map(obj -> new TopUser((Long) obj[0], (String) obj[1], (Long) obj[2]))
                .collect(Collectors.toList());

        List<Object[]> topFlashcardResults = flashcardAttemptRepository.findTopFlashcardSets(5);
        List<TopSet> mostAttemptedFlashcardSets = topFlashcardResults.stream()
                .map(obj -> new TopSet((Long) obj[0], (String) obj[1], (Long) obj[2]))
                .collect(Collectors.toList());

        List<Object[]> topQuizResults = quizAttemptRepository.findTopQuizSets(5);
        List<TopSet> mostAttemptedQuizSets = topQuizResults.stream()
                .map(obj -> new TopSet((Long) obj[0], (String) obj[1], (Long) obj[2]))
                .collect(Collectors.toList());

        return new EngagementStats(mostActiveUsers, mostAttemptedFlashcardSets, mostAttemptedQuizSets);
    }
}
