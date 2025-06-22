package com.example.demo.service;

import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.model.entity.User;
import com.example.demo.model.entity.flashcard.Flashcard;
import com.example.demo.model.entity.flashcard.FlashcardProgress;
import com.example.demo.model.entity.flashcard.FlashcardSet;
import com.example.demo.model.entity.flashcard.UserSpacedRepetitionSettings;
import com.example.demo.model.enums.CardStatus;
import com.example.demo.model.io.dto.SpacedRepetitionModeData;
import com.example.demo.model.io.dto.StudyProgressStats;
import com.example.demo.repository.FlashcardProgressRepository;
import com.example.demo.repository.FlashcardRepository;
import com.example.demo.repository.FlashcardSetRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserSpacedRepetitionSettingsRepository;
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
public class SpacedRepetitionService {

    private static final float DEFAULT_EASE_FACTOR = 2.5f;
    private static final int MINIMUM_INTERVAL = 1;

    private final FlashcardProgressRepository flashcardProgressRepository;
    private final UserSpacedRepetitionSettingsRepository settingsRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;
    private final FlashcardSetRepository flashcardSetRepository;

    // Lấy dữ liệu chế độ học
    public SpacedRepetitionModeData getModeData(Long userId, Long flashcardSetId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Optional<List<FlashcardProgress>> progressListOpt = flashcardProgressRepository.findByUser_Id(userId);
        SpacedRepetitionModeData modeData = new SpacedRepetitionModeData();

        if (progressListOpt.isEmpty() || progressListOpt.get().isEmpty()) {
            modeData.setFirstTime(true);
            modeData.setMessage("Spaced Repetition: This mode helps you retain information...");
            UserSpacedRepetitionSettings settings = settingsRepository.findByUser_Id(userId)
                    .orElseGet(() -> {
                        UserSpacedRepetitionSettings newSettings = UserSpacedRepetitionSettings.builder().user(user).build();
                        return settingsRepository.save(newSettings);
                    });
            modeData.setNewFlashcardsPerDay(settings.getNewFlashcardsPerDay() != null ? settings.getNewFlashcardsPerDay() : 10);
            modeData.setNewCardsCount(0);
            modeData.setKnowCardsCount(0);
            // Khởi tạo FlashcardProgress khi lần đầu
            initializeFlashcardProgress(userId, flashcardSetId);
        } else {
            modeData.setFirstTime(false);
            modeData.setMessage("New Day");

            List<FlashcardProgress> progressList = progressListOpt.get().stream()
                    .filter(p -> p.getFlashcard().getFlashcardSet().getId().equals(flashcardSetId))
                    .filter(p -> p.getStatus() != CardStatus.ARCHIVED)
                    .collect(Collectors.toList());

            LocalDate today = LocalDate.now();
            long newCardsCount = progressList.stream()
                    .filter(p -> p.getStatus() == CardStatus.NEW)
                    .count();
            long reviewCardsCount = progressList.stream()
                    .filter(p -> p.getNextDueDate() != null && !p.getNextDueDate().isAfter(today))
                    .filter(p -> p.getStatus() == CardStatus.KNOWN)
                    .count();

            modeData.setNewCardsCount((int) newCardsCount);
            modeData.setKnowCardsCount((int) reviewCardsCount);
        }
        return modeData;
    }

    // Cài đặt số flashcard mới mỗi ngày
    public void setNewFlashcardsPerDay(Long userId, Integer newFlashcardsPerDay) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        UserSpacedRepetitionSettings settings = settingsRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    UserSpacedRepetitionSettings newSettings = UserSpacedRepetitionSettings.builder().user(user).build();
                    return newSettings;
                });
        settings.setNewFlashcardsPerDay(newFlashcardsPerDay);
        settingsRepository.save(settings);
    }

    // Bắt đầu phiên học
    public List<Flashcard> startStudySession(Long userId, Long flashcardSetId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        UserSpacedRepetitionSettings settings = settingsRepository.findByUser_Id(userId)
                .orElseThrow(() -> new EntityNotFoundException("Settings not found"));

        FlashcardSet flashcardSet = flashcardSetRepository.findById(flashcardSetId)
                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found"));

        List<FlashcardProgress> progressList = flashcardProgressRepository.findByUserAndFlashcard_FlashcardSet(user, flashcardSet).stream()
                .filter(p -> p.getStatus() != CardStatus.ARCHIVED)
                .collect(Collectors.toList());

        List<FlashcardProgress> newProgressList = progressList.stream()
                .filter(p -> p.getStatus() == CardStatus.NEW)
                .limit(settings.getNewFlashcardsPerDay())
                .collect(Collectors.toList());

        LocalDate today = LocalDate.now();
        List<FlashcardProgress> reviewProgressList = progressList.stream()
                .filter(p -> p.getNextDueDate() != null && !p.getNextDueDate().isAfter(today))
                .filter(p -> p.getStatus() == CardStatus.KNOWN)
                .collect(Collectors.toList());

        return Stream.concat(newProgressList.stream(), reviewProgressList.stream())
                .map(FlashcardProgress::getFlashcard)
                .collect(Collectors.toList());
    }

    // Gửi kết quả ôn tập
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
            calculateNextDueDate(progress);
        } else {
            progress.setStatus(CardStatus.LEARNING);
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