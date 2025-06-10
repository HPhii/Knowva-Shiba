package com.example.demo.service.impl;

import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.mapper.QuizSetManualMapper;
import com.example.demo.model.entity.User;
import com.example.demo.model.entity.quiz.QuizAnswer;
import com.example.demo.model.entity.quiz.QuizQuestion;
import com.example.demo.model.entity.quiz.QuizSet;
import com.example.demo.model.enums.SourceType;
import com.example.demo.model.enums.QuestionType;
import com.example.demo.model.io.request.quiz.*;
import com.example.demo.model.io.response.object.quiz.QuizSetResponse;
import com.example.demo.model.io.response.object.quiz.SimplifiedQuizSetResponse;
import com.example.demo.repository.QuizSetRepository;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.IQuizSetService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizSetService implements IQuizSetService {
    private final QuizSetRepository quizSetRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final IAccountService accountService;
    private final QuizSetManualMapper quizSetMapper;

    @Value("${flask.service.url}")
    private String flaskHost;

    @Override
    public QuizSetResponse deleteQuizSetById(Long id) {
        QuizSet quizSet = quizSetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("QuizSet not found with id: " + id));
        quizSetRepository.delete(quizSet);
        return quizSetMapper.mapToQuizSetResponse(quizSet);
    }

    @Override
    public QuizSetResponse getQuizSetById(Long id) {
        QuizSet quizSet = quizSetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("QuizSet not found with id: " + id));
        return quizSetMapper.mapToQuizSetResponse(quizSet);
    }

    @Override
    public List<QuizSetResponse> getQuizSetsOfUser() {
        User user = accountService.getCurrentAccount().getUser();
        List<QuizSet> quizSets = quizSetRepository.findAllByOwner_Id(user.getId());
        return quizSetMapper.mapToQuizSetResponseList(quizSets);
    }

    @Override
    public List<QuizSetResponse> getAllQuizSets() {
        List<QuizSet> quizSets = quizSetRepository.findAll();
        return quizSetMapper.mapToQuizSetResponseList(quizSets);
    }

    @Override
    public SimplifiedQuizSetResponse generateQuizSet(CreateQuizSetRequest request, MultipartFile file) {
        User owner = accountService.getCurrentAccount().getUser();
        // Gọi Flask AI service
        String aiResponse = callFlaskAIService(file, request.getLanguage(), request.getSourceType(),
                request.getQuestionType(), request.getMaxQuestions());

        // Phân tích phản hồi từ AI
        List<QuizQuestion> questions = parseAIResponse(aiResponse, request.getMaxQuestions());

        // Tạo QuizSet tạm thời (không lưu vào database)
        QuizSet tempQuizSet = QuizSet.builder()
                .owner(owner)
                .title(request.getTitle())
                .sourceType(request.getSourceType())
                .language(request.getLanguage())
                .questionType(request.getQuestionType())
                .maxQuestions(request.getMaxQuestions())
                .visibility(request.getVisibility())
                .timeLimit(request.getTimeLimit())
                .questions(questions)
                .build();

        // Thiết lập mối quan hệ cho các câu hỏi và câu trả lời
        for (QuizQuestion question : questions) {
            question.setQuizSet(tempQuizSet);
            for (QuizAnswer answer : question.getAnswers()) {
                answer.setQuestion(question);
            }
        }

        // Trả về response mà không lưu
        return quizSetMapper.mapToSimplifiedQuizSetResponse(tempQuizSet);
    }

    @Override
    public QuizSetResponse saveQuizSet(SaveQuizSetRequest request) {
        User owner = accountService.getCurrentAccount().getUser();

        // Tạo QuizSet từ request
        QuizSet quizSet = QuizSet.builder()
                .owner(owner)
                .title(request.getTitle())
                .sourceType(request.getSourceType())
                .language(request.getLanguage())
                .questionType(request.getQuestionType())
                .maxQuestions(request.getMaxQuestions())
                .visibility(request.getVisibility())
                .timeLimit(request.getTimeLimit())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .questions(new ArrayList<>())
                .build();

        // Tạo QuizQuestion và QuizAnswer từ request
        for (SaveQuizQuestionRequest qReq : request.getQuestions()) {
            QuizQuestion question = QuizQuestion.builder()
                    .quizSet(quizSet)
                    .questionText(qReq.getQuestionText())
                    .questionHtml(qReq.getQuestionHtml())
                    .imageUrl(qReq.getImageUrl())
                    .timeLimit(qReq.getTimeLimit())
                    .order(qReq.getOrder())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .answers(new ArrayList<>())
                    .build();

            for (SaveQuizAnswerRequest aReq : qReq.getAnswers()) {
                QuizAnswer answer = QuizAnswer.builder()
                        .question(question)
                        .answerText(aReq.getAnswerText())
                        .isCorrect(aReq.getIsCorrect())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                question.getAnswers().add(answer);
            }

            quizSet.getQuestions().add(question);
        }

        quizSet = quizSetRepository.save(quizSet);

        return quizSetMapper.mapToQuizSetResponse(quizSet);
    }

    private String callFlaskAIService(MultipartFile file, String language, SourceType sourceType,
                                      QuestionType questionType, Integer maxQuestions) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            String url = "http://"+ flaskHost + ":5000/generate-quiz?language=" + language +
                    "&sourceType=" + sourceType.name() +
                    "&questionType=" + questionType.name() +
                    "&maxQuestions=" + (maxQuestions != null ? maxQuestions.toString() : "5");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            return restTemplate.postForObject(url, requestEntity, String.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to call AI service", e);
        }
    }

    private List<QuizQuestion> parseAIResponse(String aiResponse, Integer maxQuestions) {
        try {
            JsonNode rootNode = objectMapper.readTree(aiResponse);
            List<QuizQuestion> questions = new ArrayList<>();
            JsonNode questionsNode = rootNode.get("questions");

            // Giới hạn số câu hỏi theo maxQuestions nếu có
            int limit = maxQuestions != null ? Math.min(maxQuestions, questionsNode.size()) : questionsNode.size();
            for (int i = 0; i < limit; i++) {
                JsonNode qNode = questionsNode.get(i);
                QuizQuestion question = QuizQuestion.builder()
                        .questionText(qNode.get("questionText").asText())
                        .questionHtml(qNode.has("questionHtml") ? qNode.get("questionHtml").asText(null) : null)
                        .imageUrl(qNode.has("imageUrl") ? qNode.get("imageUrl").asText(null) : null)
                        .timeLimit(qNode.has("timeLimit") ? qNode.get("timeLimit").asInt() : null)
                        .order(i + 1)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .answers(new ArrayList<>())
                        .build();

                JsonNode answersNode = qNode.get("answers");
                for (JsonNode aNode : answersNode) {
                    QuizAnswer answer = QuizAnswer.builder()
                            .answerText(aNode.get("answerText").asText())
                            .isCorrect(aNode.get("isCorrect").asBoolean())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    question.getAnswers().add(answer);
                }

                questions.add(question);
            }
            return questions;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }

    @Override
    public QuizSetResponse updateQuizSet(Long quizSetId, UpdateQuizSetRequest request) {
        User user = accountService.getCurrentAccount().getUser();
        QuizSet quizSet = quizSetRepository.findById(quizSetId)
                .orElseThrow(() -> new EntityNotFoundException("QuizSet not found"));

        // Kiểm tra quyền sở hữu
        if (!quizSet.getOwner().getId().equals(user.getId())) {
            throw new SecurityException("You are not authorized to update this QuizSet");
        }

        // Cập nhật thông tin QuizSet
        quizSet.setTitle(request.getTitle());
        quizSet.setLanguage(request.getLanguage());
        quizSet.setQuestionType(request.getQuestionType());
        quizSet.setMaxQuestions(request.getMaxQuestions());
        quizSet.setVisibility(request.getVisibility());
        quizSet.setTimeLimit(request.getTimeLimit());
        quizSet.setUpdatedAt(LocalDateTime.now());

        // Cập nhật hoặc tạo mới câu hỏi
        List<QuizQuestion> updatedQuestions = new ArrayList<>();
        for (UpdateQuizQuestionRequest qReq : request.getQuestions()) {
            QuizQuestion question;
            if (qReq.getId() != null) {
                // Cập nhật câu hỏi hiện có
                question = quizSet.getQuestions().stream()
                        .filter(q -> q.getId().equals(qReq.getId()))
                        .findFirst()
                        .orElseThrow(() -> new EntityNotFoundException("Question not found"));
            } else {
                // Tạo câu hỏi mới
                question = new QuizQuestion();
                question.setQuizSet(quizSet);
                question.setCreatedAt(LocalDateTime.now());
            }

            question.setQuestionText(qReq.getQuestionText());
            question.setQuestionHtml(qReq.getQuestionHtml());
            question.setImageUrl(qReq.getImageUrl());
            question.setTimeLimit(qReq.getTimeLimit());
            question.setOrder(qReq.getOrder());
            question.setUpdatedAt(LocalDateTime.now());

            // Cập nhật hoặc tạo mới câu trả lời
            List<QuizAnswer> updatedAnswers = new ArrayList<>();
            for (UpdateQuizAnswerRequest aReq : qReq.getAnswers()) {
                QuizAnswer answer;
                if (aReq.getId() != null) {
                    // Cập nhật câu trả lời hiện có
                    answer = question.getAnswers().stream()
                            .filter(a -> a.getId().equals(aReq.getId()))
                            .findFirst()
                            .orElseThrow(() -> new EntityNotFoundException("Answer not found"));
                } else {
                    // Tạo câu trả lời mới
                    answer = new QuizAnswer();
                    answer.setQuestion(question);
                    answer.setCreatedAt(LocalDateTime.now());
                }

                answer.setAnswerText(aReq.getAnswerText());
                answer.setIsCorrect(aReq.getIsCorrect());
                answer.setUpdatedAt(LocalDateTime.now());
                updatedAnswers.add(answer);
            }
            question.setAnswers(updatedAnswers);
            updatedQuestions.add(question);
        }
        quizSet.setQuestions(updatedQuestions);

        quizSet = quizSetRepository.save(quizSet);

        return quizSetMapper.mapToQuizSetResponse(quizSet);
    }
}