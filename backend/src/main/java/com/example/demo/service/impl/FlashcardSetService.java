package com.example.demo.service.impl;

import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.mapper.FlashcardSetManualMapper;
import com.example.demo.model.entity.Account;
import com.example.demo.model.entity.User;
import com.example.demo.model.entity.flashcard.Flashcard;
import com.example.demo.model.entity.flashcard.FlashcardAccessControl;
import com.example.demo.model.entity.flashcard.FlashcardSet;
import com.example.demo.model.enums.*;
import com.example.demo.model.io.request.flashcard.*;
import com.example.demo.model.io.response.object.EmailDetails;
import com.example.demo.model.io.response.object.flashcard.ExamModeFeedbackResponse;
import com.example.demo.model.io.response.object.flashcard.FlashcardSetResponse;
import com.example.demo.model.io.response.object.flashcard.SimplifiedFlashcardSetResponse;
import com.example.demo.model.io.response.object.quiz.SimplifiedQuizSetResponse;
import com.example.demo.repository.FlashcardAccessControlRepository;
import com.example.demo.repository.FlashcardProgressRepository;
import com.example.demo.repository.FlashcardSetRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.intface.IEmailService;
import com.example.demo.service.intface.INotificationService;
import com.example.demo.service.template.FlashcardSetAIService;
import com.example.demo.service.template.FlaskAIService;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.IFlashcardSetService;
import com.example.demo.utils.InputValidationUtils;
import com.example.demo.utils.Parser;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.mail.MessagingException;
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
public class FlashcardSetService implements IFlashcardSetService {
    private final FlashcardSetRepository flashcardSetRepository;
    private final FlaskAIService flaskAIService;
    private final IAccountService accountService;
    private final FlashcardSetManualMapper flashcardSetMapper;
    private final FlashcardSetAIService flashcardSetAIService;
    private final FlashcardAccessControlRepository flashcardAccessControlRepository;
    private final UserRepository userRepository;
    private final INotificationService notificationService;

    @Override
    @Cacheable(value = "allFlashcardSets")
    public List<FlashcardSetResponse> getAllFlashcardSets() {
        User currentUser = accountService.getCurrentAccount().getUser();
        List<FlashcardSet> allFlashcardSets = flashcardSetRepository.findAll();
        return getFlashcardSetResponses(currentUser, allFlashcardSets);
    }

    private List<FlashcardSetResponse> getFlashcardSetResponses(User currentUser, List<FlashcardSet> allFlashcardSets) {
        List<FlashcardSet> accessibleFlashcardSets = allFlashcardSets.stream()
                .filter(flashcardSet -> {
                    try {
                        checkAccessPermission(flashcardSet, currentUser, null, Permission.VIEW);
                        return true;
                    } catch (SecurityException e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
        return flashcardSetMapper.mapToFlashcardSetResponseList(accessibleFlashcardSets);
    }

    @Override
    @Cacheable(value = "flashcardSetsOfUser", key = "#userId")
    public List<FlashcardSetResponse> getFlashcardSetsOfUser(Long userId) {
        User currentUser = accountService.getCurrentAccount().getUser();
        List<FlashcardSet> flashcardSets = flashcardSetRepository.findAllByOwner_Id(userId);
        return getFlashcardSetResponses(currentUser, flashcardSets);
    }
    
    @Override
    @Cacheable(value = "flashcardSetsByCategory", key = "#category")
    public List<FlashcardSetResponse> getFlashcardSetsByCategory(Category category) {
        User currentUser = accountService.getCurrentAccount().getUser();
        List<FlashcardSet> flashcardSets = flashcardSetRepository.findAllByCategory(category);
        return getFlashcardSetResponses(currentUser, flashcardSets);
    }

    @Override
    public SimplifiedFlashcardSetResponse generateFlashcardSet(CreateFlashcardSetRequest request, List<MultipartFile> files, String text) {
        Account ownerAccount = accountService.getCurrentAccount();
        Object input = InputValidationUtils.validateInput(ownerAccount, text, files, request.getMaxFlashcards());

        List<Flashcard> flashcards = flashcardSetAIService.generateFromAI(
                input,
                request.getLanguage(),
                request.getSourceType().name(),
                request.getMaxFlashcards()
        );

        FlashcardSet tempFlashcardSet = FlashcardSet.builder()
                .owner(ownerAccount.getUser())
                .title(request.getTitle())
                .sourceType(request.getSourceType())
                .language(request.getLanguage())
                .cardType(request.getCardType())
                .visibility(request.getVisibility())
                .category(request.getCategory())
                .flashcards(flashcards)
                .build();

        for (Flashcard flashcard : flashcards) {
            flashcard.setFlashcardSet(tempFlashcardSet);
        }

        return flashcardSetMapper.mapToSimplifiedFlashcardSetResponse(tempFlashcardSet);
    }

    @Override
    @CacheEvict(value = {"flashcardSet", "flashcardSetsOfUser", "allFlashcardSets", "flashcardSetsByCategory"}, allEntries = true)
    public FlashcardSetResponse saveFlashcardSet(SaveFlashcardSetRequest request) {
        User owner = accountService.getCurrentAccount().getUser();
        FlashcardSet flashcardSet = FlashcardSet.builder()
                .owner(owner)
                .title(request.getTitle())
                .sourceType(request.getSourceType())
                .language(request.getLanguage())
                .cardType(request.getCardType())
                .visibility(request.getVisibility())
                .category(request.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .flashcards(new ArrayList<>())
                .build();

        if (request.getVisibility() == Visibility.HIDDEN) {
            flashcardSet.setAccessToken(UUID.randomUUID().toString());
        } else {
            flashcardSet.setAccessToken(null);
        }

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
    @CacheEvict(value = {"flashcardSet", "flashcardSetsByCategory"}, key = "#flashcardSetId")
    public FlashcardSetResponse updateFlashcardSet(Long flashcardSetId, UpdateFlashcardSetRequest request, String token) {
        User currentUser = accountService.getCurrentAccount().getUser();
        FlashcardSet flashcardSet = flashcardSetRepository.findById(flashcardSetId)
                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found"));

        checkAccessPermission(flashcardSet, currentUser, token, Permission.EDIT);

        flashcardSet.setTitle(request.getTitle());
        flashcardSet.setSourceType(request.getSourceType());
        flashcardSet.setLanguage(request.getLanguage());
        flashcardSet.setCardType(request.getCardType());
        flashcardSet.setVisibility(request.getVisibility());
        flashcardSet.setCategory(request.getCategory());
        flashcardSet.setUpdatedAt(LocalDateTime.now());

        if (request.getVisibility() == Visibility.HIDDEN) {
            flashcardSet.setAccessToken(UUID.randomUUID().toString());
        } else {
            flashcardSet.setAccessToken(null);
        }

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
    public FlashcardSetResponse getFlashcardSetById(Long id, String token) {
        User currentUser = accountService.getCurrentAccount().getUser();
        FlashcardSet flashcardSet = flashcardSetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found"));

        checkAccessPermission(flashcardSet, currentUser, token, Permission.VIEW);
        return flashcardSetMapper.mapToFlashcardSetResponse(flashcardSet);
    }

    @Override
    @CacheEvict(value = "flashcardSet", key = "#id")
    public FlashcardSetResponse deleteFlashcardSetById(Long id) {
        User currentUser = accountService.getCurrentAccount().getUser();
        FlashcardSet flashcardSet = flashcardSetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found"));

        if (!flashcardSet.getOwner().getId().equals(currentUser.getId())) {
            throw new SecurityException("Only the owner can delete this FlashcardSet");
        }

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

    @Override
    public void inviteUserToFlashcardSet(Long flashcardSetId, Long invitedUserId, Permission permission) {
        User owner = accountService.getCurrentAccount().getUser();
        FlashcardSet flashcardSet = flashcardSetRepository.findById(flashcardSetId)
                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet not found"));

        // check if the current user is the owner of the flashcard set
        if (!flashcardSet.getOwner().getId().equals(owner.getId())) {
            throw new SecurityException("You are not authorized to invite users to this FlashcardSet");
        }

        // check if the invited user exists
        User invitedUser = userRepository.findById(invitedUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // create or update the access control entry
        FlashcardAccessControl accessControl = FlashcardAccessControl.builder()
                .flashcardSet(flashcardSet)
                .invitedUser(invitedUser)
                .permission(permission)
                .invitedAt(LocalDateTime.now())
                .build();

        String message = owner.getFullName() + " đã mời bạn vào set " + flashcardSet.getTitle() +
                " để cùng học tập, ôn luyện và chinh phục kiến thức!";
        notificationService.createNotification(invitedUserId, NotificationType.FLASHCARD_INVITE, message, flashcardSetId);

        flashcardAccessControlRepository.save(accessControl);
//
//        String subject = "Bạn đã được mời vào FlashcardSet";
//        String templateName = "invite-flashcard-set";
//        Map<String, Object> contextVariables = new HashMap<>();
//        contextVariables.put("userName", invitedUser.getFullName());
//        contextVariables.put("flashcardSetTitle", flashcardSet.getTitle());
//        contextVariables.put("permission", permission.name());
//
//        EmailDetails emailDetails = new EmailDetails();
//        emailDetails.setReceiver(invitedUser.getAccount());
//        emailDetails.setSubject(subject);
//
//        try {
//            emailService.sendMail(emailDetails, templateName, contextVariables);
//        } catch (MessagingException e) {
//            System.out.println("Lỗi gửi email cho user: " + invitedUser.getId());
//        }
    }

    private void checkAccessPermission(FlashcardSet flashcardSet, User currentUser, String token, Permission requiredPermission) {
        if (flashcardSet.getOwner().getId().equals(currentUser.getId())) return;

        if (flashcardSet.getVisibility() == Visibility.PUBLIC) {
            if (requiredPermission == Permission.EDIT)
                throw new SecurityException("You do not have edit permission for this FlashcardSet");
            return;
        }

//        if (flashcardSet.getVisibility() == Visibility.HIDDEN) {
//            if (token == null || !token.equals(flashcardSet.getAccessToken()))
//                throw new SecurityException("Invalid token for hidden FlashcardSet");
//            if (requiredPermission == Permission.EDIT)
//                throw new SecurityException("Edit not allowed for hidden FlashcardSet");
//            return;
//        }

        if (flashcardSet.getVisibility() == Visibility.HIDDEN) {
            if (token == null || !token.equals(flashcardSet.getAccessToken()))
                throw new SecurityException("Invalid token for hidden FlashcardSet");
            if (requiredPermission == Permission.EDIT) {
                Optional<FlashcardAccessControl> access = flashcardAccessControlRepository
                        .findByFlashcardSetAndInvitedUser(flashcardSet, currentUser);
                if (access.isEmpty() || access.get().getPermission() != Permission.EDIT)
                    throw new SecurityException("You do not have edit permission for this hidden FlashcardSet");
            }
            return;
        }

        if (flashcardSet.getVisibility() == Visibility.PRIVATE) {
            Optional<FlashcardAccessControl> access = flashcardAccessControlRepository
                    .findByFlashcardSetAndInvitedUser(flashcardSet, currentUser);
            if (access.isEmpty() || access.get().getPermission().ordinal() < requiredPermission.ordinal())
                throw new SecurityException("You do not have sufficient permission");
            return;
        }

        throw new SecurityException("Access denied");
    }

}