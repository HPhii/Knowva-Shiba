package com.example.demo.service.impl;

import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.mapper.QuizSetManualMapper;
import com.example.demo.model.entity.Account;
import com.example.demo.model.entity.User;
import com.example.demo.model.entity.quiz.QuizAccessControl;
import com.example.demo.model.entity.quiz.QuizAnswer;
import com.example.demo.model.entity.quiz.QuizQuestion;
import com.example.demo.model.entity.quiz.QuizSet;
import com.example.demo.model.enums.Permission;
import com.example.demo.model.enums.Role;
import com.example.demo.model.enums.Visibility;
import com.example.demo.model.io.request.quiz.*;
import com.example.demo.model.io.response.object.quiz.QuizSetResponse;
import com.example.demo.model.io.response.object.quiz.SimplifiedQuizSetResponse;
import com.example.demo.repository.QuizAccessControlRepository;
import com.example.demo.repository.QuizSetRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.template.QuizSetAIService;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.IQuizSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizSetService implements IQuizSetService {
    private final QuizSetRepository quizSetRepository;
    private final IAccountService accountService;
    private final QuizSetManualMapper quizSetMapper;
    private final QuizSetAIService quizSetAIService;
    private final QuizAccessControlRepository quizAccessControlRepository;
    private final UserRepository userRepository;

    @Override
    @CacheEvict(value = "quizSet", key = "#id")
    public QuizSetResponse deleteQuizSetById(Long id) {
        User currentUser = accountService.getCurrentAccount().getUser();
        QuizSet quizSet = quizSetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("QuizSet not found with id: " + id));

        if (!quizSet.getOwner().getId().equals(currentUser.getId())) {
            throw new SecurityException("Only the owner can delete this QuizSet");
        }

        quizSetRepository.delete(quizSet);
        return quizSetMapper.mapToQuizSetResponse(quizSet);
    }

    @Override
    @Cacheable(value = "quizSet", key = "#id")
    public QuizSetResponse getQuizSetById(Long id, String token) {
        User currentUser = accountService.getCurrentAccount().getUser();
        QuizSet quizSet = quizSetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("QuizSet not found with id: " + id));

        checkAccessPermission(quizSet, currentUser, token, Permission.VIEW);
        return quizSetMapper.mapToQuizSetResponse(quizSet);
    }

    @Override
    @Cacheable(value = "quizSetsOfUser", key = "#userId")
    public List<QuizSetResponse> getQuizSetsOfUser(Long userId) {
        List<QuizSet> quizSets = quizSetRepository.findAllByOwner_Id(userId);
        return quizSetMapper.mapToQuizSetResponseList(quizSets);
    }

    @Override
    @Cacheable(value = "allQuizSets")
    public List<QuizSetResponse> getAllQuizSets() {
        User currentUser = accountService.getCurrentAccount().getUser();
        List<QuizSet> allQuizSets = quizSetRepository.findAll();
        List<QuizSet> accessibleQuizSets = allQuizSets.stream()
                .filter(quizSet -> {
                    try {
                        checkAccessPermission(quizSet, currentUser, null, Permission.VIEW);
                        return true;
                    } catch (SecurityException e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
        return quizSetMapper.mapToQuizSetResponseList(accessibleQuizSets);
    }

    @Override
    public SimplifiedQuizSetResponse generateQuizSet(CreateQuizSetRequest request, List<MultipartFile> files, String text) {
        Account ownerAccount = accountService.getCurrentAccount();

        if (ownerAccount.getRole() == Role.REGULAR && (files != null && !files.isEmpty())) {
            throw new SecurityException("User with REGULAR role can only use text input, not file uploads.");
        }

        Object input;
        if (text != null && !text.isBlank()) {
            input = text;
        } else if (files != null && !files.isEmpty()) {
            input = files;
        } else {
            throw new IllegalArgumentException("Either text or files must be provided.");
        }

        List<QuizQuestion> questions = quizSetAIService.generateFromAI(
                input,
                request.getLanguage(),
                request.getSourceType().name(),
                request.getMaxQuestions()
        );

        QuizSet tempQuizSet = QuizSet.builder()
                .owner(ownerAccount.getUser())
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
    @CacheEvict(value = {"quizSet", "quizSetsOfUser", "allQuizSets"}, allEntries = true)
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

        if (request.getVisibility() == Visibility.HIDDEN) {
            quizSet.setAccessToken(UUID.randomUUID().toString());
        } else {
            quizSet.setAccessToken(null);
        }

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
    @CacheEvict(value = "quizSet", key = "#quizSetId")
    public QuizSetResponse updateQuizSet(Long quizSetId, UpdateQuizSetRequest request) {
        User currentUser = accountService.getCurrentAccount().getUser();
        QuizSet quizSet = quizSetRepository.findById(quizSetId)
                .orElseThrow(() -> new EntityNotFoundException("QuizSet not found"));

        checkAccessPermission(quizSet, currentUser, null, Permission.EDIT);

        updateQuizSetInfo(quizSet, request);

        if (request.getVisibility() == Visibility.HIDDEN) {
            quizSet.setAccessToken(UUID.randomUUID().toString());
        } else {
            quizSet.setAccessToken(null);
        }

        List<QuizQuestion> updatedQuestions = updateQuestions(quizSet, request.getQuestions());
        quizSet.setQuestions(updatedQuestions);

        quizSet = quizSetRepository.save(quizSet);
        return quizSetMapper.mapToQuizSetResponse(quizSet);
    }

    @Override
    public void inviteUserToQuizSet(Long quizSetId, Long invitedUserId, Permission permission) {
        User owner = accountService.getCurrentAccount().getUser();
        QuizSet quizSet = quizSetRepository.findById(quizSetId)
                .orElseThrow(() -> new EntityNotFoundException("QuizSet not found"));

        if (!quizSet.getOwner().getId().equals(owner.getId())) {
            throw new SecurityException("You are not authorized to invite users to this QuizSet");
        }

        User invitedUser = userRepository.findById(invitedUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        QuizAccessControl accessControl = QuizAccessControl.builder()
                .quizSet(quizSet)
                .invitedUser(invitedUser)
                .permission(permission)
                .invitedAt(LocalDateTime.now())
                .build();

        quizAccessControlRepository.save(accessControl);
    }

    private void checkAccessPermission(QuizSet quizSet, User currentUser, String token, Permission requiredPermission) {
        if (quizSet.getOwner().getId().equals(currentUser.getId())) {
            return;
        }

        if (quizSet.getVisibility() == Visibility.PUBLIC) {
            if (requiredPermission == Permission.EDIT) {
                throw new SecurityException("You do not have edit permission for this QuizSet");
            }
            return;
        }

        if (quizSet.getVisibility() == Visibility.HIDDEN) {
            if (token == null || !token.equals(quizSet.getAccessToken())) {
                throw new SecurityException("Invalid token for hidden QuizSet");
            }
            if (requiredPermission == Permission.EDIT) {
                Optional<QuizAccessControl> access = quizAccessControlRepository
                        .findByQuizSetAndInvitedUser(quizSet, currentUser);
                if (access.isEmpty() || access.get().getPermission() != Permission.EDIT) {
                    throw new SecurityException("You do not have edit permission for this hidden QuizSet");
                }
            }
            return;
        }

        if (quizSet.getVisibility() == Visibility.PRIVATE) {
            Optional<QuizAccessControl> access = quizAccessControlRepository
                    .findByQuizSetAndInvitedUser(quizSet, currentUser);
            if (access.isEmpty() || access.get().getPermission().ordinal() < requiredPermission.ordinal()) {
                throw new SecurityException("You do not have sufficient permission");
            }
            return;
        }

        throw new SecurityException("Access denied");
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