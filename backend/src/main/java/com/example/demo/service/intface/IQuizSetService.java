package com.example.demo.service.intface;

import com.example.demo.model.enums.Category;
import com.example.demo.model.enums.Permission;
import com.example.demo.model.io.request.quiz.CreateQuizSetRequest;
import com.example.demo.model.io.request.quiz.SaveQuizSetRequest;
import com.example.demo.model.io.request.quiz.UpdateQuizSetRequest;
import com.example.demo.model.io.response.object.quiz.QuizSetResponse;
import com.example.demo.model.io.response.object.quiz.SimplifiedQuizSetResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IQuizSetService {
    SimplifiedQuizSetResponse generateQuizSet(CreateQuizSetRequest request, List<MultipartFile> file, String text);
    QuizSetResponse saveQuizSet(SaveQuizSetRequest request);
    QuizSetResponse getQuizSetById(Long id, String token);
    List<QuizSetResponse> getQuizSetsOfUser(Long userId);
    List<QuizSetResponse> getAllQuizSets();
    QuizSetResponse deleteQuizSetById(Long id);
    QuizSetResponse updateQuizSet(Long quizSetId, UpdateQuizSetRequest request, String token);
    void inviteUserToQuizSet(Long quizSetId, Long invitedUserId, Permission permission);
    List<QuizSetResponse> getQuizSetsByCategory(Category category);
}
