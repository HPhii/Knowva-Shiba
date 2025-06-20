package com.example.demo.service.impl;

import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.mapper.FlashcardSetManualMapper;
import com.example.demo.model.entity.User;
import com.example.demo.model.entity.flashcard.*;
import com.example.demo.model.enums.SourceType;
import com.example.demo.model.enums.CardType;
import com.example.demo.model.io.request.flashcard.*;
import com.example.demo.model.io.response.object.flashcard.FlashcardSetResponse;
import com.example.demo.model.io.response.object.flashcard.SimplifiedFlashcardResponse;
import com.example.demo.model.io.response.object.flashcard.SimplifiedFlashcardSetResponse;
import com.example.demo.model.io.response.object.quiz.SimplifiedQuizAnswerResponse;
import com.example.demo.model.io.response.object.quiz.SimplifiedQuizQuestionResponse;
import com.example.demo.model.io.response.object.quiz.SimplifiedQuizSetResponse;
import com.example.demo.repository.FlashcardSetRepository;
import com.example.demo.repository.FlashcardProgressRepository;
import com.example.demo.repository.FlashcardAttemptRepository;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.IFlashcardSetService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlashcardSetService implements IFlashcardSetService {
    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardProgressRepository flashcardProgressRepository;
    private final FlashcardAttemptRepository flashcardAttemptRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final IAccountService accountService;
    private final FlashcardSetManualMapper flashcardSetMapper;

    @Value("${flask.service.url}")
    private String flaskHost;

    @Override
    public List<FlashcardSetResponse> getAllFlashcardSets() {
        return flashcardSetMapper.mapToFlashcardSetResponseList(flashcardSetRepository.findAll());
    }

    @Override
    public List<FlashcardSetResponse> getFlashcardSetsOfUser() {
        User user = accountService.getCurrentAccount().getUser();
        List<FlashcardSet> flashcardSets = flashcardSetRepository.findAllByOwner_Id(user.getId());
        return flashcardSetMapper.mapToFlashcardSetResponseList(flashcardSets);
    }

    @Override
    public SimplifiedFlashcardSetResponse generateFlashcardSet(CreateFlashcardSetRequest request, MultipartFile file, String text) {
        User owner = accountService.getCurrentAccount().getUser();
        String aiResponse = callFlaskAIService(file, text, request.getLanguage(), request.getSourceType(),
                request.getCardType(), request.getMaxFlashcards());
        List<Flashcard> flashcards = parseAIResponse(aiResponse, request.getMaxFlashcards());
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
    public FlashcardSetResponse getFlashcardSetById(Long id) {
        FlashcardSet flashcardSet = flashcardSetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found"));
        return flashcardSetMapper.mapToFlashcardSetResponse(flashcardSet);
    }

    @Override
    public FlashcardSetResponse deleteFlashcardSetById(Long id) {
        FlashcardSet flashcardSet = flashcardSetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found"));
        flashcardSetRepository.delete(flashcardSet);
        return flashcardSetMapper.mapToFlashcardSetResponse(flashcardSet);
    }

    // Exam Mode: Chấm điểm tự luận bằng AI (implementing)
    @Override
    public FlashcardAttempt examModeSubmit(Long flashcardSetId, Long flashcardId, String userAnswer) {
//        User user = accountService.getCurrentAccount().getUser();
//        FlashcardSet flashcardSet = flashcardSetRepository.findById(flashcardSetId)
//                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found"));
//        Flashcard flashcard = flashcardSet.getFlashcards().stream()
//                .filter(f -> f.getId().equals(flashcardId))
//                .findFirst()
//                .orElseThrow(() -> new EntityNotFoundException("Flashcard not found"));
//
//        String aiFeedback = callFlaskAIForExamMode(flashcard.getBack(), userAnswer);
//        JsonNode feedbackNode = parseAIResponse(aiFeedback, null);
//        Float score = feedbackNode.get("score").floatValue();
//
//        FlashcardAttempt attempt = FlashcardAttempt.builder()
//                .user(user)
//                .flashcardSet(flashcardSet)
//                .flashcard(flashcard)
//                .userAnswer(userAnswer)
//                .score(score)
//                .attemptedAt(LocalDateTime.now())
//                .build();
//        return flashcardAttemptRepository.save(attempt);
        return null;
    }

    // Space Repetition Mode: Lên lịch học và test (implementing)
    @Override
    public List<Flashcard> spaceRepetitionMode(Long flashcardSetId, Integer dailyLimit) {
        User user = accountService.getCurrentAccount().getUser();
        FlashcardSet flashcardSet = flashcardSetRepository.findById(flashcardSetId)
                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found"));

        List<FlashcardProgress> progressList = flashcardProgressRepository.findByUserAndFlashcard_FlashcardSet(user, flashcardSet);
        List<Flashcard> dueFlashcards = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Flashcard flashcard : flashcardSet.getFlashcards()) {
            FlashcardProgress progress = progressList.stream()
                    .filter(p -> p.getFlashcard().getId().equals(flashcard.getId()))
                    .findFirst()
                    .orElse(FlashcardProgress.builder()
                            .user(user)
                            .flashcard(flashcard)
                            .easeFactor(2.5f)
                            .repetitionCount(0)
                            .nextDueDate(today)
                            .build());

            if (progress.getNextDueDate().isBefore(today.plusDays(1))) {
                dueFlashcards.add(flashcard);
                progress.setLastReviewedAt(LocalDateTime.now());
                progress.setRepetitionCount(progress.getRepetitionCount() + 1);
                progress.setNextDueDate(today.plusDays(calculateInterval(progress.getRepetitionCount(), progress.getEaseFactor())));
                flashcardProgressRepository.save(progress);
            }
        }
        return dueFlashcards.stream().limit(dailyLimit).collect(Collectors.toList());
    }

    @Override
    public SimplifiedQuizSetResponse generateQuizMode(Long flashcardSetId, String language, String questionType, int maxQuestions) {
        FlashcardSet flashcardSet = flashcardSetRepository.findById(flashcardSetId)
                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found"));
        String aiResponse = callFlaskAIForQuizGeneration(flashcardSet, language, questionType, maxQuestions);
        return parseQuizAIResponse(aiResponse);
    }

    private String callFlaskAIForQuizGeneration(FlashcardSet flashcardSet, String language, String questionType, int maxQuestions) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<SimplifiedFlashcardResponse> simplifiedFlashcards = flashcardSet.getFlashcards().stream()
                .map(flashcard -> new SimplifiedFlashcardResponse(
                        flashcard.getFront(),
                        flashcard.getBack(),
                        flashcard.getImageUrl(),
                        flashcard.getOrder()
                ))
                .collect(Collectors.toList());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Ensure LocalDateTime support
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.set("flashcards", objectMapper.valueToTree(simplifiedFlashcards));

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);
        String url = String.format("http://%s/generate-quiz-from-flashcards?language=%s&questionType=%s&maxQuestions=%d",
                flaskHost, language, questionType, maxQuestions);
        return restTemplate.postForObject(url, requestEntity, String.class);
    }

    private String callFlaskAIService(MultipartFile file, String textInput, String language, SourceType sourceType,
                                      CardType cardType, Integer maxFlashcards) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            if (textInput != null && !textInput.isBlank()) {
                body.add("text", textInput);
            } else if (file != null && !file.isEmpty()) {
                body.add("file", new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                });
            } else {
                throw new IllegalArgumentException("Either file or text must be provided.");
            }
            String url = "http://" + flaskHost + "/generate-flashcards?language=" + language +
                    "&sourceType=" + sourceType.name() +
                    "&cardType=" + cardType.name() +
                    "&maxFlashcards=" + (maxFlashcards != null ? maxFlashcards : 5);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            return restTemplate.postForObject(url, requestEntity, String.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to call AI service", e);
        }
    }

    // Gọi Flask AI để chấm điểm Exam Mode
    private String callFlaskAIForExamMode(String correctAnswer, String userAnswer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestBody = String.format("{\"correctAnswer\": \"%s\", \"userAnswer\": \"%s\"}", correctAnswer, userAnswer);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        String url = "http://" + flaskHost + "/exam-mode-grade";
        return restTemplate.postForObject(url, requestEntity, String.class);
    }

    private List<Flashcard> parseAIResponse(String aiResponse, Integer maxFlashcards) {
        try {
            JsonNode rootNode = objectMapper.readTree(aiResponse);
            List<Flashcard> flashcards = new ArrayList<>();
            JsonNode flashcardsNode = rootNode.get("flashcards");
            int limit = maxFlashcards != null ? Math.min(maxFlashcards, flashcardsNode.size()) : flashcardsNode.size();
            for (int i = 0; i < limit; i++) {
                JsonNode fNode = flashcardsNode.get(i);
                Flashcard flashcard = Flashcard.builder()
                        .front(fNode.get("front").asText())
                        .back(fNode.get("back").asText())
                        .imageUrl(fNode.has("imageUrl") ? fNode.get("imageUrl").asText(null) : null)
                        .order(i + 1)
                        .build();
                flashcards.add(flashcard);
            }
            return flashcards;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }

    private SimplifiedQuizSetResponse parseQuizAIResponse(String aiResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(aiResponse);
            JsonNode questionsNode = rootNode.get("questions");

            List<SimplifiedQuizQuestionResponse> questions = new ArrayList<>();
            for (JsonNode qNode : questionsNode) {
                List<SimplifiedQuizAnswerResponse> answers = new ArrayList<>();
                for (JsonNode aNode : qNode.get("answers")) {
                    answers.add(new SimplifiedQuizAnswerResponse(
                            aNode.get("answerText").asText(),
                            aNode.get("isCorrect").asBoolean()
                    ));
                }
                questions.add(new SimplifiedQuizQuestionResponse(
                        qNode.get("questionText").asText(),
                        qNode.has("questionHtml") ? qNode.get("questionHtml").asText(null) : null,
                        qNode.has("imageUrl") ? qNode.get("imageUrl").asText(null) : null,
                        qNode.has("timeLimit") ? qNode.get("timeLimit").asInt() : null,
                        questions.size() + 1, // Order tăng dần
                        answers
                ));
            }

            return new SimplifiedQuizSetResponse(
                    "Generated Quiz from Flashcards",
                    "FLASHCARD",
                    "en",
                    "MULTIPLE_CHOICE",
                    questions.size(),
                    "PUBLIC",
                    30 * questions.size(),
                    questions
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }

    private int calculateInterval(int repetitionCount, float easeFactor) {
        if (repetitionCount == 1) return 1;
        if (repetitionCount == 2) return 6;
        return (int) (calculateInterval(repetitionCount - 1, easeFactor) * easeFactor);
    }
}
