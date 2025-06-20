package com.example.demo.service.intface;

import com.example.demo.model.entity.flashcard.Flashcard;
import com.example.demo.model.entity.flashcard.FlashcardAttempt;
import com.example.demo.model.io.request.flashcard.CreateFlashcardSetRequest;
import com.example.demo.model.io.request.flashcard.SaveFlashcardSetRequest;
import com.example.demo.model.io.request.flashcard.UpdateFlashcardSetRequest;
import com.example.demo.model.io.response.object.flashcard.FlashcardSetResponse;
import com.example.demo.model.io.response.object.flashcard.SimplifiedFlashcardSetResponse;
import com.example.demo.model.io.response.object.quiz.SimplifiedQuizSetResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IFlashcardSetService {
    SimplifiedFlashcardSetResponse generateFlashcardSet(CreateFlashcardSetRequest request, MultipartFile file, String text);
    FlashcardSetResponse saveFlashcardSet(SaveFlashcardSetRequest request);
    FlashcardSetResponse updateFlashcardSet(Long flashcardSetId, UpdateFlashcardSetRequest request);
    FlashcardSetResponse getFlashcardSetById(Long id);
    FlashcardSetResponse deleteFlashcardSetById(Long id);
    List<FlashcardSetResponse> getFlashcardSetsOfUser();
    List<FlashcardSetResponse> getAllFlashcardSets();
    FlashcardAttempt examModeSubmit(Long flashcardSetId, Long flashcardId, String userAnswer);
    List<Flashcard> spaceRepetitionMode(Long flashcardSetId, Integer dailyLimit);
    SimplifiedQuizSetResponse generateQuizMode(Long flashcardSetId, String language, String questionType, int maxQuestions);
}
