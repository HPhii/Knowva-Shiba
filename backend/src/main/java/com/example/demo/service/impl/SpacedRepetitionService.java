package com.example.demo.service.impl;

import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.model.entity.User;
import com.example.demo.model.entity.flashcard.*;
import com.example.demo.model.enums.ActivityType;
import com.example.demo.model.enums.CardStatus;
import com.example.demo.model.io.dto.PerformanceStats;
import com.example.demo.model.io.dto.SpacedRepetitionModeData;
import com.example.demo.model.io.dto.StudyProgressStats;
import com.example.demo.repository.*;
import com.example.demo.service.intface.ISpacedRepetitionService;
import com.example.demo.service.intface.IActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SpacedRepetitionService implements ISpacedRepetitionService {
    private static final float DEFAULT_EASE_FACTOR = 2.5f;
    private static final int MINIMUM_INTERVAL = 1;

    private static final String SPACED_REP = "Lặp lại ngắt quãng: Chế độ này giúp bạn ghi nhớ thông tin bằng cách xem lại các thẻ nhớ ở những khoảng thời gian tối ưu.";
    private static final String NEW_DAY = "Hôm nay là ngày mới, hãy bắt đầu ôn tập!";
    private static final int MAX_INTERVAL = 365;

    private final FlashcardProgressRepository flashcardProgressRepository;
    private final FlashcardSetProgressSettingsRepository flashcardSetProgressSettingsRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardAttemptRepository flashcardAttemptRepository;
    private final IActivityLogService activityLogService;

    @Override
    public SpacedRepetitionModeData getModeData(Long userId, Long flashcardSetId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        FlashcardSet flashcardSet = flashcardSetRepository.findById(flashcardSetId)
                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found"));

        SpacedRepetitionModeData modeData = new SpacedRepetitionModeData();
        List<FlashcardProgress> progressList = flashcardProgressRepository.findByUserAndFlashcard_FlashcardSet(user, flashcardSet);

        if (progressList.isEmpty()) {
            modeData.setFirstTime(true);
            modeData.setMessage(SPACED_REP);
            FlashcardSetProgressSettings settings = flashcardSetProgressSettingsRepository.findByUserAndFlashcardSet(user, flashcardSet)
                    .orElseGet(() -> {
                        FlashcardSetProgressSettings newSettings = FlashcardSetProgressSettings.builder()
                                .user(user)
                                .flashcardSet(flashcardSet)
                                .newFlashcardsPerDay(10)
                                .build();
                        return flashcardSetProgressSettingsRepository.save(newSettings);
                    });
            modeData.setNewFlashcardsPerDay(settings.getNewFlashcardsPerDay());
            modeData.setNewCardsCount(0);
            modeData.setKnowCardsCount(0);
            initializeFlashcardProgress(userId, flashcardSetId);
        } else {
            FlashcardSetProgressSettings settings = flashcardSetProgressSettingsRepository.findByUserAndFlashcardSet(user, flashcardSet)
                    .orElseThrow(() -> new EntityNotFoundException("Settings not found"));
            modeData.setNewFlashcardsPerDay(settings.getNewFlashcardsPerDay());
            modeData.setFirstTime(false);
            modeData.setMessage(NEW_DAY);
            LocalDate today = LocalDate.now();

            long newCardsCount = flashcardProgressRepository.countByUserAndFlashcard_FlashcardSetAndStatus(user, flashcardSet, CardStatus.NEW);
            long reviewCardsCount = flashcardProgressRepository.countByUserAndFlashcard_FlashcardSetAndStatusAndNextDueDateBefore(
                    user, flashcardSet, CardStatus.KNOWN, today);
            modeData.setNewCardsCount((int) newCardsCount);
            modeData.setKnowCardsCount((int) reviewCardsCount);
        }
        return modeData;
    }

    // Cài đặt số flashcard mới mỗi ngày cho một bộ flashcard cụ thể
    @Override
    public void setNewFlashcardsPerDay(Long userId, Long flashcardSetId, Integer newFlashcardsPerDay) {
        if (newFlashcardsPerDay == null || newFlashcardsPerDay < 0) {
            throw new IllegalArgumentException("New flashcards per day must be a non-negative integer.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        FlashcardSet flashcardSet = flashcardSetRepository.findById(flashcardSetId)
                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found"));

        FlashcardSetProgressSettings settings = flashcardSetProgressSettingsRepository.findByUserAndFlashcardSet(user, flashcardSet)
                .orElseGet(() -> FlashcardSetProgressSettings.builder()
                        .user(user)
                        .flashcardSet(flashcardSet)
                        .newFlashcardsPerDay(newFlashcardsPerDay)
                        .build());
        settings.setNewFlashcardsPerDay(newFlashcardsPerDay);
        flashcardSetProgressSettingsRepository.save(settings);
    }

    // Bắt đầu phiên học
//    @Override
//    public List<Flashcard> startStudySession(Long userId, Long flashcardSetId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new EntityNotFoundException("User not found"));
//        FlashcardSet flashcardSet = flashcardSetRepository.findById(flashcardSetId)
//                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found"));
//
//        FlashcardSetProgressSettings settings = flashcardSetProgressSettingsRepository.findByUserAndFlashcardSet(user, flashcardSet)
//                .orElseThrow(() -> new EntityNotFoundException("Settings not found"));
//
//        List<FlashcardProgress> progressList = flashcardProgressRepository.findByUserAndFlashcard_FlashcardSet(user, flashcardSet).stream()
//                .filter(p -> p.getStatus() != CardStatus.ARCHIVED)
//                .collect(Collectors.toList());
//
//        List<FlashcardProgress> newProgressList = getNewFlashcards(progressList, settings.getNewFlashcardsPerDay());
//        List<FlashcardProgress> reviewProgressList = getReviewFlashcards(progressList, LocalDate.now());
//
//        List<Flashcard> result = Stream.concat(newProgressList.stream(), reviewProgressList.stream())
//                .map(FlashcardProgress::getFlashcard)
//                .collect(Collectors.toList());
//
//        if (result.isEmpty()) {
//            throw new IllegalStateException("No flashcards available for study session.");
//        }
//
//        return result;
//    }

    @Override
    public List<Flashcard> startStudySession(Long userId, Long flashcardSetId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        FlashcardSet flashcardSet = flashcardSetRepository.findById(flashcardSetId)
                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found"));

        FlashcardSetProgressSettings settings = flashcardSetProgressSettingsRepository.findByUserAndFlashcardSet(user, flashcardSet)
                .orElseThrow(() -> new EntityNotFoundException("Settings not found for this user and flashcard set."));

        List<FlashcardProgress> progressList = flashcardProgressRepository.findByUserAndFlashcard_FlashcardSet(user, flashcardSet).stream()
                .filter(p -> p.getStatus() != CardStatus.ARCHIVED)
                .collect(Collectors.toList());

        List<FlashcardProgress> reviewProgressList = getReviewFlashcards(progressList, LocalDate.now());

        LocalDate today = LocalDate.now();
        long newCardsStudiedTodayCount = progressList.stream()
                .filter(p -> p.getStatus() != CardStatus.NEW &&
                        p.getLastReviewedAt() != null &&
                        p.getLastReviewedAt().toLocalDate().isEqual(today))
                .count();

        int newFlashcardsPerDay = settings.getNewFlashcardsPerDay();
        long remainingNewCardLimit = newFlashcardsPerDay - newCardsStudiedTodayCount;
        if (remainingNewCardLimit < 0) {
            remainingNewCardLimit = 0;
        }

        List<FlashcardProgress> newProgressList = getNewFlashcards(progressList, (int) remainingNewCardLimit);


        List<Flashcard> result = Stream.concat(reviewProgressList.stream(), newProgressList.stream())
                .map(FlashcardProgress::getFlashcard)
                .distinct()
                .collect(Collectors.toList());

        if (result.isEmpty() && !progressList.isEmpty()) {
            throw new IllegalStateException("No flashcards available for study session today. Please come back tomorrow!");
        }

        return result;
    }

    private List<FlashcardProgress> getNewFlashcards(List<FlashcardProgress> progressList, int limit) {
        return progressList.stream()
                .filter(p -> p.getStatus() == CardStatus.NEW)
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<FlashcardProgress> getReviewFlashcards(List<FlashcardProgress> progressList, LocalDate today) {
        return progressList.stream()
                .filter(p -> p.getNextDueDate() != null && !p.getNextDueDate().isAfter(today))
                .filter(p -> p.getStatus() == CardStatus.KNOWN)
                .collect(Collectors.toList());
    }

    // Gửi kết quả ôn tập
    @Override
    public StudyProgressStats submitReview(Long userId, Long flashcardId, Long flashcardSetId, int quality) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new EntityNotFoundException("Flashcard not found"));

        FlashcardProgress progress = flashcardProgressRepository.findByUserAndFlashcard(user, flashcard)
                .orElse(FlashcardProgress.builder().user(user).flashcard(flashcard).build());

        updateFlashcardProgress(progress, quality);
        flashcardProgressRepository.save(progress);

        List<FlashcardProgress> progressList = flashcardProgressRepository.findByUserAndFlashcard_FlashcardSet(user, flashcard.getFlashcardSet()).stream()
                .filter(p -> p.getStatus() != CardStatus.ARCHIVED)
                .collect(Collectors.toList());
        long unstudiedCount = progressList.stream().filter(p -> p.getStatus() == CardStatus.NEW).count();
        long dontKnowCount = progressList.stream().filter(p -> p.getStatus() == CardStatus.LEARNING).count();
        long knowCount = progressList.stream().filter(p -> p.getStatus() == CardStatus.KNOWN).count();

        StudyProgressStats stats = new StudyProgressStats();
        stats.setUnstudiedCount((int) unstudiedCount);
        stats.setDontKnowCount((int) dontKnowCount);
        stats.setKnowCount((int) knowCount);

        // === GHI LOG HOẠT ĐỘNG KHI HOÀN THÀNH PHIÊN HỌC ===
        // Chỉ ghi log khi không còn thẻ nào cần ôn tập trong ngày (tránh spam log)
        if (unstudiedCount == 0 && dontKnowCount == 0) {
            activityLogService.logActivity(user, ActivityType.STUDY_FLASHCARD_SET, "", flashcard.getFlashcardSet().getId());
        }
        // =================================================

        return stats;
    }

    @Override
    public void updateFlashcardProgress(FlashcardProgress progress, int quality) {
        if (progress.getRepetitionCount() == null || progress.getEaseFactor() == null || progress.getInterval() == null) {
            progress.setRepetitionCount(0);
            progress.setEaseFactor(DEFAULT_EASE_FACTOR);
            progress.setInterval(MINIMUM_INTERVAL);
            progress.setStatus(CardStatus.NEW);
        }

        progress.setLastReviewedAt(LocalDateTime.now());

        if (quality >= 3) {
            int n = progress.getRepetitionCount() + 1;
            progress.setRepetitionCount(n);
            progress.setEaseFactor(calculateEaseFactor(progress.getEaseFactor(), quality));
            int previousInterval = progress.getInterval();
            int newInterval = calculateInterval(n, progress.getEaseFactor(), previousInterval);
            progress.setInterval(newInterval);

            progress.setNextDueDate(progress.getLastReviewedAt().toLocalDate().plusDays(progress.getInterval()));
            progress.setStatus(CardStatus.KNOWN);
        } else {
            progress.setRepetitionCount(0);
            if (quality == 0) {
                progress.setEaseFactor(Math.max(1.3f, progress.getEaseFactor() - 0.3f));
            } else {
                progress.setEaseFactor(Math.max(1.3f, progress.getEaseFactor() - 0.2f));
            }
            progress.setInterval(MINIMUM_INTERVAL);
            progress.setNextDueDate(progress.getLastReviewedAt().toLocalDate().plusDays(1));
            progress.setStatus(CardStatus.LEARNING);
        }

        FlashcardAttempt attempt = FlashcardAttempt.builder()
                .user(progress.getUser())
                .flashcard(progress.getFlashcard())
                .flashcardProgress(progress)
                .attemptDate(LocalDateTime.now())
                .result(quality >= 3 ? FlashcardAttempt.AttemptResult.KNOW : FlashcardAttempt.AttemptResult.DONT_KNOW)
                .build();
        flashcardAttemptRepository.save(attempt);
    }

    private float calculateEaseFactor(float currentEase, int quality) {
        float newEase = currentEase + (0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f));
        return Math.max(1.3f, newEase);
    }

    private int calculateInterval(int repetitionCount, float easeFactor, int lastInterval) {
        if (repetitionCount == 1) return 1;       // first time → 1 day
        if (repetitionCount == 2) return 6;       // second time → 6 days
        int interval = (int) (lastInterval * easeFactor);  // subsequent times → last interval * ease factor
        return Math.min(interval, MAX_INTERVAL);
    }



    @Override
    public List<FlashcardAttempt> getStudyHistory(Long userId, Long flashcardSetId) {
        return flashcardAttemptRepository.findByUserIdAndFlashcard_FlashcardSet_Id(userId, flashcardSetId);
    }

    @Override
    public PerformanceStats getPerformanceStats(Long userId, Long flashcardSetId) {
        List<FlashcardAttempt> attempts = flashcardAttemptRepository.findByUserIdAndFlashcard_FlashcardSet_Id(userId, flashcardSetId);
        long knowCount = attempts.stream().filter(a -> a.getResult() == FlashcardAttempt.AttemptResult.KNOW).count();
        long total = attempts.size();
        double retentionRate = total > 0 ? (double) knowCount / total * 100 : 0;

        PerformanceStats stats = new PerformanceStats();
        stats.setRetentionRate(retentionRate);
        stats.setTotalAttempts(total);
        return stats;
    }

    private void initializeFlashcardProgress(Long userId, Long flashcardSetId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        FlashcardSet flashcardSet = flashcardSetRepository.findById(flashcardSetId)
                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found"));

        List<Flashcard> flashcards = flashcardRepository.findByFlashcardSet(flashcardSet);
        List<FlashcardProgress> existingProgress = flashcardProgressRepository.findByUserAndFlashcard_FlashcardSet(user, flashcardSet);

        List<Flashcard> uninitializedFlashcards = flashcards.stream()
                .filter(f -> existingProgress.stream().noneMatch(p -> p.getFlashcard().getId().equals(f.getId())))
                .collect(Collectors.toList());

        List<FlashcardProgress> newProgressList = uninitializedFlashcards.stream()
                .map(f -> FlashcardProgress.builder()
                        .user(user)
                        .flashcard(f)
                        .status(CardStatus.NEW)
                        .lastReviewedAt(null)
                        .easeFactor(DEFAULT_EASE_FACTOR)
                        .repetitionCount(0)
                        .interval(MINIMUM_INTERVAL)
                        .nextDueDate(null)
                        .build())
                .collect(Collectors.toList());

        flashcardProgressRepository.saveAll(newProgressList);
    }
}