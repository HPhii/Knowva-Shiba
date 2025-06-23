package com.example.demo.service.impl;

import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.model.entity.User;
import com.example.demo.model.entity.flashcard.*;
import com.example.demo.model.enums.CardStatus;
import com.example.demo.model.io.dto.SpacedRepetitionModeData;
import com.example.demo.model.io.dto.StudyProgressStats;
import com.example.demo.repository.*;
import com.example.demo.service.intface.ISpacedRepetitionService;
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

    private final FlashcardProgressRepository flashcardProgressRepository;
    private final FlashcardSetProgressSettingsRepository flashcardSetProgressSettingsRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;
    private final FlashcardSetRepository flashcardSetRepository;

    // Lấy dữ liệu chế độ học
    @Override
    public SpacedRepetitionModeData getModeData(Long userId, Long flashcardSetId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        FlashcardSet flashcardSet = flashcardSetRepository.findById(flashcardSetId)
                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found"));

        SpacedRepetitionModeData modeData = new SpacedRepetitionModeData();
        Optional<List<FlashcardProgress>> progressListOpt = flashcardProgressRepository.findByUser_Id(userId);

        if (progressListOpt.isEmpty() || progressListOpt.get().isEmpty()) {
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
    @Override
    public List<Flashcard> startStudySession(Long userId, Long flashcardSetId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        FlashcardSet flashcardSet = flashcardSetRepository.findById(flashcardSetId)
                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found"));

        FlashcardSetProgressSettings settings = flashcardSetProgressSettingsRepository.findByUserAndFlashcardSet(user, flashcardSet)
                .orElseThrow(() -> new EntityNotFoundException("Settings not found"));

        List<FlashcardProgress> progressList = flashcardProgressRepository.findByUserAndFlashcard_FlashcardSet(user, flashcardSet).stream()
                .filter(p -> p.getStatus() != CardStatus.ARCHIVED)
                .collect(Collectors.toList());

        List<FlashcardProgress> newProgressList = getNewFlashcards(progressList, settings.getNewFlashcardsPerDay());
        List<FlashcardProgress> reviewProgressList = getReviewFlashcards(progressList, LocalDate.now());

        return Stream.concat(newProgressList.stream(), reviewProgressList.stream())
                .map(FlashcardProgress::getFlashcard)
                .collect(Collectors.toList());
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
    public StudyProgressStats submitReview(Long userId, Long flashcardId, Long flashcardSetId, Boolean knowsCard) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new EntityNotFoundException("Flashcard not found"));

        FlashcardProgress progress = flashcardProgressRepository.findByUserAndFlashcard(user, flashcard)
                .orElse(FlashcardProgress.builder().user(user).flashcard(flashcard).build());

        updateFlashcardProgress(progress, knowsCard);
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

        return stats;
    }

    @Override
    public void updateFlashcardProgress(FlashcardProgress progress, boolean knowsCard) {
        if (progress.getRepetitionCount() == null) {
            progress.setRepetitionCount(0);
            progress.setEaseFactor(DEFAULT_EASE_FACTOR);
            progress.setStatus(CardStatus.NEW);
        }

        progress.setLastReviewedAt(LocalDateTime.now());
        progress.setRepetitionCount(progress.getRepetitionCount() + 1);

        if (knowsCard) {
            progress.setStatus(CardStatus.KNOWN);
            progress.setEaseFactor(progress.getEaseFactor() + 0.1f);
            calculateNextDueDate(progress);
        } else {
            progress.setStatus(CardStatus.LEARNING);
            progress.setEaseFactor(Math.max(1.3f, progress.getEaseFactor() - 0.2f));
            progress.setRepetitionCount(0);
            progress.setNextDueDate(LocalDate.now().plusDays(MINIMUM_INTERVAL));
        }
    }

    private void calculateNextDueDate(FlashcardProgress progress) {
        int interval;
        switch (progress.getRepetitionCount()) {
            case 1:
                interval = 1; // Ôn lại sau 1 ngày
                break;
            case 2:
                interval = 6; // Ôn lại sau 6 ngày
                break;
            default:
                interval = (int) ((progress.getRepetitionCount() - 1) * progress.getEaseFactor());
                break;
        }
        progress.setNextDueDate(LocalDate.now().plusDays(interval));
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
                        .nextDueDate(null)
                        .build())
                .collect(Collectors.toList());

        flashcardProgressRepository.saveAll(newProgressList);
    }
}