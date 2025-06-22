package com.example.demo.service.impl;

import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.mapper.QuizSetManualMapper;
import com.example.demo.model.entity.User;
import com.example.demo.model.entity.quiz.QuizAnswer;
import com.example.demo.model.entity.quiz.QuizQuestion;
import com.example.demo.model.entity.quiz.QuizSet;
import com.example.demo.model.io.request.quiz.*;
import com.example.demo.model.io.response.object.quiz.QuizSetResponse;
import com.example.demo.model.io.response.object.quiz.SimplifiedQuizSetResponse;
import com.example.demo.repository.QuizSetRepository;
import com.example.demo.service.QuizSetAIService;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.IQuizSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizSetService implements IQuizSetService {
    private final QuizSetRepository quizSetRepository;
    private final IAccountService accountService;
    private final QuizSetManualMapper quizSetMapper;
    private final QuizSetAIService quizSetAIService;

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
    public SimplifiedQuizSetResponse generateQuizSet(CreateQuizSetRequest request, List<MultipartFile> files, String text) {
        User owner = accountService.getCurrentAccount().getUser();

        Object input = text != null && !text.isBlank() ? text : files;

        List<QuizQuestion> questions = quizSetAIService.generateFromAI(
                input,
                request.getLanguage(),
                request.getSourceType().name(),
                request.getMaxQuestions()
        );

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

        for (QuizQuestion question : questions) {
            question.setQuizSet(tempQuizSet);
            for (QuizAnswer answer : question.getAnswers()) {
                answer.setQuestion(question);
            }
        }

        return quizSetMapper.mapToSimplifiedQuizSetResponse(tempQuizSet);
    }

    @Override
    public QuizSetResponse saveQuizSet(SaveQuizSetRequest request) {
        User owner = accountService.getCurrentAccount().getUser();

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

    @Override
    public QuizSetResponse updateQuizSet(Long quizSetId, UpdateQuizSetRequest request) {
        User user = accountService.getCurrentAccount().getUser();
        QuizSet quizSet = findAndValidateQuizSetOwner(quizSetId, user);

        updateQuizSetInfo(quizSet, request);

        List<QuizQuestion> updatedQuestions = updateQuestions(quizSet, request.getQuestions());
        quizSet.setQuestions(updatedQuestions);

        quizSet = quizSetRepository.save(quizSet);
        return quizSetMapper.mapToQuizSetResponse(quizSet);
    }

    private QuizSet findAndValidateQuizSetOwner(Long quizSetId, User user) {
        QuizSet quizSet = quizSetRepository.findById(quizSetId)
                .orElseThrow(() -> new EntityNotFoundException("QuizSet not found"));

        if (!quizSet.getOwner().getId().equals(user.getId())) {
            throw new SecurityException("You are not authorized to update this QuizSet");
        }
        return quizSet;
    }

    private void updateQuizSetInfo(QuizSet quizSet, UpdateQuizSetRequest request) {
        quizSet.setTitle(request.getTitle());
        quizSet.setLanguage(request.getLanguage());
        quizSet.setQuestionType(request.getQuestionType());
        quizSet.setMaxQuestions(request.getMaxQuestions());
        quizSet.setVisibility(request.getVisibility());
        quizSet.setTimeLimit(request.getTimeLimit());
        quizSet.setUpdatedAt(LocalDateTime.now());
    }

    private List<QuizQuestion> updateQuestions(QuizSet quizSet, List<UpdateQuizQuestionRequest> questionRequests) {
        List<QuizQuestion> updatedQuestions = new ArrayList<>();
        for (UpdateQuizQuestionRequest qReq : questionRequests) {
            QuizQuestion question = findOrCreateQuestion(quizSet, qReq);
            updateQuestionFields(question, qReq);

            List<QuizAnswer> updatedAnswers = updateAnswers(question, qReq.getAnswers());
            question.setAnswers(updatedAnswers);

            updatedQuestions.add(question);
        }
        return updatedQuestions;
    }

    private QuizQuestion findOrCreateQuestion(QuizSet quizSet, UpdateQuizQuestionRequest qReq) {
        if (qReq.getId() != null) {
            return quizSet.getQuestions().stream()
                    .filter(q -> q.getId().equals(qReq.getId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Question not found"));
        } else {
            QuizQuestion question = new QuizQuestion();
            question.setQuizSet(quizSet);
            question.setCreatedAt(LocalDateTime.now());
            return question;
        }
    }

    private void updateQuestionFields(QuizQuestion question, UpdateQuizQuestionRequest qReq) {
        question.setQuestionText(qReq.getQuestionText());
        question.setQuestionHtml(qReq.getQuestionHtml());
        question.setImageUrl(qReq.getImageUrl());
        question.setTimeLimit(qReq.getTimeLimit());
        question.setOrder(qReq.getOrder());
        question.setUpdatedAt(LocalDateTime.now());
    }

    private List<QuizAnswer> updateAnswers(QuizQuestion question, List<UpdateQuizAnswerRequest> answerRequests) {
        List<QuizAnswer> updatedAnswers = new ArrayList<>();
        for (UpdateQuizAnswerRequest aReq : answerRequests) {
            QuizAnswer answer = findOrCreateAnswer(question, aReq);
            answer.setAnswerText(aReq.getAnswerText());
            answer.setIsCorrect(aReq.getIsCorrect());
            answer.setUpdatedAt(LocalDateTime.now());
            updatedAnswers.add(answer);
        }
        return updatedAnswers;
    }

    private QuizAnswer findOrCreateAnswer(QuizQuestion question, UpdateQuizAnswerRequest aReq) {
        if (aReq.getId() != null) {
            return question.getAnswers().stream()
                    .filter(a -> a.getId().equals(aReq.getId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Answer not found"));
        } else {
            QuizAnswer answer = new QuizAnswer();
            answer.setQuestion(question);
            answer.setCreatedAt(LocalDateTime.now());
            return answer;
        }
    }
}