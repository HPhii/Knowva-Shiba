package com.example.demo.service.intface;

import com.example.demo.model.entity.flashcard.Flashcard;
import com.example.demo.model.enums.Category;
import com.example.demo.model.enums.Permission;
import com.example.demo.model.io.request.flashcard.CreateFlashcardSetRequest;
import com.example.demo.model.io.request.flashcard.SaveFlashcardSetRequest;
import com.example.demo.model.io.request.flashcard.SubmitExamModeRequest;
import com.example.demo.model.io.request.flashcard.UpdateFlashcardSetRequest;
import com.example.demo.model.io.response.object.InvitedUserResponse;
import com.example.demo.model.io.response.object.flashcard.ExamModeFeedbackResponse;
import com.example.demo.model.io.response.object.flashcard.FlashcardSetResponse;
import com.example.demo.model.io.response.object.flashcard.SimplifiedFlashcardSetResponse;
import com.example.demo.model.io.response.object.quiz.SimplifiedQuizSetResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IFlashcardSetService {
    SimplifiedFlashcardSetResponse generateFlashcardSet(CreateFlashcardSetRequest request, List<MultipartFile> files, String text);
    FlashcardSetResponse saveFlashcardSet(SaveFlashcardSetRequest request);
    FlashcardSetResponse updateFlashcardSet(Long flashcardSetId, UpdateFlashcardSetRequest request, String token);
    FlashcardSetResponse getFlashcardSetById(Long id, String token);
    FlashcardSetResponse deleteFlashcardSetById(Long id);
    List<FlashcardSetResponse> getFlashcardSetsOfUser(Long userId);
    List<FlashcardSetResponse> getAllFlashcardSets();
    ExamModeFeedbackResponse submitExamMode(Long flashcardSetId, SubmitExamModeRequest request);
    SimplifiedQuizSetResponse generateQuizMode(Long flashcardSetId, String language, String questionType, int maxQuestions);
    void inviteUserToFlashcardSet(Long flashcardSetId, Long invitedUserId, Permission permission);
    List<FlashcardSetResponse> getFlashcardSetsByCategory(Category category);
    List<InvitedUserResponse> getInvitedUsers(Long flashcardSetId);
}
