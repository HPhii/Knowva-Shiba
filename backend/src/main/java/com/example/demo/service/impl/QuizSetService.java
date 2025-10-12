package com.example.demo.service.impl;

import com.example.demo.exception.AuthException;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.mapper.QuizSetManualMapper;
import com.example.demo.model.entity.Account;
import com.example.demo.model.entity.User;
import com.example.demo.model.entity.quiz.QuizAccessControl;
import com.example.demo.model.entity.quiz.QuizAnswer;
import com.example.demo.model.entity.quiz.QuizQuestion;
import com.example.demo.model.entity.quiz.QuizSet;
import com.example.demo.model.enums.Category;
import com.example.demo.model.enums.NotificationType;
import com.example.demo.model.enums.Permission;
import com.example.demo.model.enums.Visibility;
import com.example.demo.model.enums.ActivityType;
import com.example.demo.model.io.request.quiz.*;
import com.example.demo.model.io.response.object.InvitedUserResponse;
import com.example.demo.model.io.response.object.quiz.QuizSetResponse;
import com.example.demo.model.io.response.object.quiz.SimplifiedQuizSetResponse;
import com.example.demo.repository.QuizAccessControlRepository;
import com.example.demo.repository.QuizSetRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.intface.IInvitationEmailService;
import com.example.demo.service.intface.INotificationService;
import com.example.demo.service.intface.IActivityLogService;
import com.example.demo.service.template.QuizSetAIService;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.IQuizSetService;
import com.example.demo.utils.InputValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
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
    private final INotificationService notificationService;
    private final IInvitationEmailService invitationEmailService;
    private final IActivityLogService activityLogService;

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
        User currentUser = accountService.getCurrentAccount().getUser();
        List<QuizSet> quizSets = quizSetRepository.findAllByOwner_Id(userId);
        return getQuizSetResponses(currentUser, quizSets);
    }

    @Override
    public List<QuizSetResponse> getAllQuizSets() {
        User currentUser = null;
        try {
            currentUser = accountService.getCurrentAccount().getUser();
        } catch (AuthException e) {
            // Bỏ qua lỗi, người dùng này là anonymous, currentUser vẫn là null
        }
        List<QuizSet> allQuizSets = quizSetRepository.findAll();
        return getQuizSetResponses(currentUser, allQuizSets);
    }

    private List<QuizSetResponse> getQuizSetResponses(User currentUser, List<QuizSet> allQuizSets) {
        List<QuizSet> accessibleQuizSets;
        if (currentUser == null) {
            accessibleQuizSets = allQuizSets.stream()
                    .filter(quizSet -> quizSet.getVisibility() == Visibility.PUBLIC)
                    .collect(Collectors.toList());
        } else {
            accessibleQuizSets = allQuizSets.stream()
                    .filter(quizSet -> {
                        try {
                            checkAccessPermission(quizSet, currentUser, null, Permission.VIEW);
                            return true;
                        } catch (SecurityException e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }
        return quizSetMapper.mapToQuizSetResponseList(accessibleQuizSets);
    }

    @Override
    @Cacheable(value = "quizSetsByCategory", key = "#category")
    public List<QuizSetResponse> getQuizSetsByCategory(Category category) {
        User currentUser = accountService.getCurrentAccount().getUser();
        List<QuizSet> quizSets = quizSetRepository.findAllByCategory(category);
        return getQuizSetResponses(currentUser, quizSets);
    }

    @Override
    public SimplifiedQuizSetResponse generateQuizSet(CreateQuizSetRequest request, List<MultipartFile> files, String text) {
        Account ownerAccount = accountService.getCurrentAccount();
        Object input = InputValidationUtils.validateInput(ownerAccount, text, files, request.getMaxQuestions());

        List<QuizQuestion> questions = quizSetAIService.generateFromAI(
                input,
                request.getLanguage(),
                request.getSourceType().name(),
                request.getMaxQuestions()
        );

        QuizSet tempQuizSet = QuizSet.builder()
                .owner(ownerAccount.getUser())
                .title(request.getTitle())
                .description(request.getDescription())
                .sourceType(request.getSourceType())
                .language(request.getLanguage())
                .questionType(request.getQuestionType())
                .maxQuestions(request.getMaxQuestions())
                .visibility(request.getVisibility())
                .category(request.getCategory())
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
    @CacheEvict(value = {"quizSet", "quizSetsOfUser", "allQuizSets", "quizSetsByCategory"}, allEntries = true)
    public QuizSetResponse saveQuizSet(SaveQuizSetRequest request) {
        User owner = accountService.getCurrentAccount().getUser();

        QuizSet quizSet = QuizSet.builder()
                .owner(owner)
                .title(request.getTitle())
                .description(request.getDescription())
                .sourceType(request.getSourceType())
                .language(request.getLanguage())
                .questionType(request.getQuestionType())
                .maxQuestions(request.getMaxQuestions())
                .visibility(request.getVisibility())
                .category(request.getCategory())
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
            boolean hasCorrectAnswer = qReq.getAnswers().stream()
                    .anyMatch(answer -> Boolean.TRUE.equals(answer.getIsCorrect()));
            if (!hasCorrectAnswer) {
                throw new IllegalArgumentException("Each question must have at least one correct answer.");
            }

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

        QuizSet savedQuizSet = quizSetRepository.save(quizSet);

        // === GHI LOG HOẠT ĐỘNG ===
        activityLogService.logActivity(owner, ActivityType.CREATE_QUIZ_SET, "", savedQuizSet.getId());
        // ============================

        return quizSetMapper.mapToQuizSetResponse(savedQuizSet);
    }

    @Override
    @CacheEvict(value = {"quizSet", "quizSetsByCategory"}, key = "#quizSetId")
    public QuizSetResponse updateQuizSet(Long quizSetId, UpdateQuizSetRequest request, String token) {
        User currentUser = accountService.getCurrentAccount().getUser();
        QuizSet quizSet = quizSetRepository.findById(quizSetId)
                .orElseThrow(() -> new EntityNotFoundException("QuizSet not found"));

        checkAccessPermission(quizSet, currentUser, token, Permission.EDIT);

        updateQuizSetInfo(quizSet, request);

        if (request.getVisibility() == Visibility.HIDDEN) {
            quizSet.setAccessToken(UUID.randomUUID().toString());
        } else {
            quizSet.setAccessToken(null);
        }

        updateQuestions(quizSet, request.getQuestions());

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

        String message = owner.getFullName() + " đã mời bạn vào set " + quizSet.getTitle() +
                " để cùng học tập, ôn luyện và chinh phục kiến thức!";
        notificationService.createNotification(invitedUserId, NotificationType.QUIZ_INVITE, message, quizSetId);

        invitationEmailService.sendInvitationEmail(
                owner,
                invitedUser,
                quizSet.getId(),
                quizSet.getTitle(),
                "Quiz",
                permission
        );

        quizAccessControlRepository.save(accessControl);
    }

    @Override
    public List<InvitedUserResponse> getInvitedUsers(Long quizSetId) {
        User currentUser = accountService.getCurrentAccount().getUser();
        QuizSet quizSet = quizSetRepository.findById(quizSetId)
                .orElseThrow(() -> new EntityNotFoundException("QuizSet not found"));

        if (!quizSet.getOwner().getId().equals(currentUser.getId())) {
            throw new SecurityException("Only the owner can view the list of invited users.");
        }

        List<QuizAccessControl> accessControls = quizAccessControlRepository.findAllByQuizSetId(quizSetId);

        return accessControls.stream()
                .map(ac -> InvitedUserResponse.builder()
                        .userId(ac.getInvitedUser().getId())
                        .username(ac.getInvitedUser().getAccount().getUsername())
                        .avatarUrl(ac.getInvitedUser().getAvatarUrl())
                        .permission(ac.getPermission())
                        .build())
                .collect(Collectors.toList());
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
        quizSet.setDescription(request.getDescription());
        quizSet.setLanguage(request.getLanguage());
        quizSet.setQuestionType(request.getQuestionType());
        quizSet.setMaxQuestions(request.getMaxQuestions());
        quizSet.setVisibility(request.getVisibility());
        quizSet.setCategory(request.getCategory());
        quizSet.setTimeLimit(request.getTimeLimit());
        quizSet.setUpdatedAt(LocalDateTime.now());
    }

    private void updateQuestions(QuizSet quizSet, List<UpdateQuizQuestionRequest> questionRequests) {
        List<Long> requestQuestionIds = questionRequests.stream()
                .map(UpdateQuizQuestionRequest::getId)
                .filter(Objects::nonNull)
                .toList();

        quizSet.getQuestions().removeIf(q -> !requestQuestionIds.contains(q.getId()));

        for (UpdateQuizQuestionRequest qReq : questionRequests) {
            if (qReq.getAnswers() == null || qReq.getAnswers().isEmpty()) {
                throw new IllegalArgumentException("Each question must have at least one answer");
            }

            boolean hasCorrectAnswer = qReq.getAnswers().stream()
                    .anyMatch(answer -> Boolean.TRUE.equals(answer.getIsCorrect()));
            if (!hasCorrectAnswer) {
                throw new IllegalArgumentException("Each question must have at least one correct answer.");
            }

            QuizQuestion question = findOrCreateQuestion(quizSet, qReq);
            updateQuestionFields(question, qReq);
            updateAnswers(question, qReq.getAnswers());

            if (qReq.getId() == null) {
                quizSet.getQuestions().add(question);
            }
        }
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

    private void updateAnswers(QuizQuestion question, List<UpdateQuizAnswerRequest> answerRequests) {
        List<Long> requestAnswerIds = answerRequests.stream()
                .map(UpdateQuizAnswerRequest::getId)
                .filter(Objects::nonNull)
                .toList();

        question.getAnswers().removeIf(a -> !requestAnswerIds.contains(a.getId()));

        for (UpdateQuizAnswerRequest aReq : answerRequests) {
            QuizAnswer answer = findOrCreateAnswer(question, aReq);
            answer.setAnswerText(aReq.getAnswerText());
            answer.setIsCorrect(aReq.getIsCorrect());
            answer.setUpdatedAt(LocalDateTime.now());

            if (aReq.getId() == null) {
                question.getAnswers().add(answer);
            }
        }
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
