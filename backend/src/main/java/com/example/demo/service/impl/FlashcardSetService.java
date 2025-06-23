package com.example.demo.service.impl;

import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.mapper.FlashcardSetManualMapper;
import com.example.demo.model.entity.User;
import com.example.demo.model.entity.flashcard.Flashcard;
import com.example.demo.model.entity.flashcard.FlashcardSet;
import com.example.demo.model.io.request.flashcard.*;
import com.example.demo.model.io.response.object.flashcard.ExamModeFeedbackResponse;
import com.example.demo.model.io.response.object.flashcard.FlashcardSetResponse;
import com.example.demo.model.io.response.object.flashcard.SimplifiedFlashcardSetResponse;
import com.example.demo.model.io.response.object.quiz.SimplifiedQuizSetResponse;
import com.example.demo.repository.FlashcardProgressRepository;
import com.example.demo.repository.FlashcardSetRepository;
import com.example.demo.service.FlashcardSetAIService;
import com.example.demo.service.FlaskAIService;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.IFlashcardSetService;
import com.example.demo.utils.Parser;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlashcardSetService implements IFlashcardSetService {
    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardProgressRepository flashcardProgressRepository;
    private final FlaskAIService flaskAIService;
    private final IAccountService accountService;
    private final FlashcardSetManualMapper flashcardSetMapper;
    private final FlashcardSetAIService flashcardSetAIService;

    @Override
    @Cacheable(value = "allFlashcardSets")
    public List<FlashcardSetResponse> getAllFlashcardSets() {
        return flashcardSetMapper.mapToFlashcardSetResponseList(flashcardSetRepository.findAll());
    }

    @Override
    @Cacheable(value = "flashcardSetsOfUser", key = "#userId")
    public List<FlashcardSetResponse> getFlashcardSetsOfUser(Long userId) {
        User user = accountService.getCurrentAccount().getUser();
        List<FlashcardSet> flashcardSets = flashcardSetRepository.findAllByOwner_Id(user.getId());
        return flashcardSetMapper.mapToFlashcardSetResponseList(flashcardSets);
    }

    @Override
    public SimplifiedFlashcardSetResponse generateFlashcardSet(CreateFlashcardSetRequest request, List<MultipartFile> files, String text) {
        User owner = accountService.getCurrentAccount().getUser();

        Object input = text != null && !text.isBlank() ? text : files;

        List<Flashcard> flashcards = flashcardSetAIService.generateFromAI(
                input,
                request.getLanguage(),
                request.getSourceType().name(),
                request.getMaxFlashcards()
        );

        FlashcardSet tempFlashcardSet = FlashcardSet.builder()
                .owner(owner)
                .title(request.getTitle())
                .sourceType(request.getSourceType())
                .language(request.getLanguage())
                .cardType(request.getCardType())
                .visibility(request.getVisibility())
                .flashcards(flashcards)
                .build();

        for (Flashcard flashcard : flashcards) {
            flashcard.setFlashcardSet(tempFlashcardSet);
        }

        return flashcardSetMapper.mapToSimplifiedFlashcardSetResponse(tempFlashcardSet);
    }

    @Override
    @CacheEvict(value = {"flashcardSet", "flashcardSetsOfUser", "allFlashcardSets"}, allEntries = true)
    public FlashcardSetResponse saveFlashcardSet(SaveFlashcardSetRequest request) {
        User owner = accountService.getCurrentAccount().getUser();
        FlashcardSet flashcardSet = FlashcardSet.builder()
                .owner(owner)
                .title(request.getTitle())
                .sourceType(request.getSourceType())
                .language(request.getLanguage())
                .cardType(request.getCardType())
                .visibility(request.getVisibility())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .flashcards(new ArrayList<>())
                .build();
        for (SaveFlashcardRequest fReq : request.getFlashcards()) {
            Flashcard flashcard = Flashcard.builder()
                    .flashcardSet(flashcardSet)
                    .front(fReq.getFront())
                    .back(fReq.getBack())
                    .imageUrl(fReq.getImageUrl())
                    .order(fReq.getOrder())
                    .build();
            flashcardSet.getFlashcards().add(flashcard);
        }
        flashcardSet = flashcardSetRepository.save(flashcardSet);
        return flashcardSetMapper.mapToFlashcardSetResponse(flashcardSet);
    }

    @Override
    @CacheEvict(value = "flashcardSet", key = "#flashcardSetId")
    public FlashcardSetResponse updateFlashcardSet(Long flashcardSetId, UpdateFlashcardSetRequest request) {
        User user = accountService.getCurrentAccount().getUser();
        FlashcardSet flashcardSet = flashcardSetRepository.findById(flashcardSetId)
                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found"));
        if (!flashcardSet.getOwner().getId().equals(user.getId())) {
            throw new SecurityException("You are not authorized to update this FlashcardSet");
        }
        flashcardSet.setTitle(request.getTitle());
        flashcardSet.setSourceType(request.getSourceType());
        flashcardSet.setLanguage(request.getLanguage());
        flashcardSet.setCardType(request.getCardType());
        flashcardSet.setVisibility(request.getVisibility());
        flashcardSet.setUpdatedAt(LocalDateTime.now());
        List<Flashcard> updatedFlashcards = new ArrayList<>();
        for (UpdateFlashcardRequest fReq : request.getFlashcards()) {
            Flashcard flashcard;
            if (fReq.getId() != null) {
                flashcard = flashcardSet.getFlashcards().stream()
                        .filter(f -> f.getId().equals(fReq.getId()))
                        .findFirst()
                        .orElseThrow(() -> new EntityNotFoundException("Flashcard not found"));
            } else {
                flashcard = new Flashcard();
                flashcard.setFlashcardSet(flashcardSet);
            }
            flashcard.setFront(fReq.getFront());
            flashcard.setBack(fReq.getBack());
            flashcard.setImageUrl(fReq.getImageUrl());
            flashcard.setOrder(fReq.getOrder());
            updatedFlashcards.add(flashcard);
        }
        flashcardSet.setFlashcards(updatedFlashcards);
        flashcardSet = flashcardSetRepository.save(flashcardSet);
        return flashcardSetMapper.mapToFlashcardSetResponse(flashcardSet);
    }

    @Override
    @Cacheable(value = "flashcardSet", key = "#id")
    public FlashcardSetResponse getFlashcardSetById(Long id) {
        FlashcardSet flashcardSet = flashcardSetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found"));
        return flashcardSetMapper.mapToFlashcardSetResponse(flashcardSet);
    }

    @Override
    @CacheEvict(value = "flashcardSet", key = "#id")
    public FlashcardSetResponse deleteFlashcardSetById(Long id) {
        FlashcardSet flashcardSet = flashcardSetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found"));
        flashcardSetRepository.delete(flashcardSet);
        return flashcardSetMapper.mapToFlashcardSetResponse(flashcardSet);
    }

    @Override
    public ExamModeFeedbackResponse submitExamMode(Long flashcardSetId, SubmitExamModeRequest request) {
        FlashcardSet flashcardSet = flashcardSetRepository.findById(flashcardSetId)
                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found"));
        Flashcard flashcard = flashcardSet.getFlashcards().stream()
                .filter(f -> f.getId().equals(request.getFlashcardId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Flashcard not found"));

        String correctAnswer = flashcard.getBack();
        String userAnswer = request.getUserAnswer();

        String aiResponse = flaskAIService.evaluateAnswer(correctAnswer, userAnswer);
        JsonNode aiFeedbackNode = Parser.parseJson(aiResponse);

        Float score = aiFeedbackNode.get("score").floatValue();
        JsonNode feedback = aiFeedbackNode.get("feedback");

        String whatWasCorrect = feedback.has("whatWasCorrect") ? feedback.get("whatWasCorrect").asText() : null;
        String whatWasIncorrect = feedback.has("whatWasIncorrect") ? feedback.get("whatWasIncorrect").asText() : null;
        String whatCouldHaveIncluded = feedback.has("whatCouldHaveIncluded") ? feedback.get("whatCouldHaveIncluded").asText() : null;

        return new ExamModeFeedbackResponse(score, whatWasCorrect, whatWasIncorrect, whatCouldHaveIncluded);
    }

    @Override
    public SimplifiedQuizSetResponse generateQuizMode(Long flashcardSetId, String language, String questionType, int maxQuestions) {
        FlashcardSet flashcardSet = flashcardSetRepository.findById(flashcardSetId)
                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found"));
        String aiResponse = flaskAIService.callFlaskAIForQuizGeneration(flashcardSet, language, questionType, maxQuestions);
        return Parser.parseQuiz(aiResponse);
    }
}
