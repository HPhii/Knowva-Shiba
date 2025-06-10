package com.example.demo.service.intface;

import com.example.demo.model.io.request.quiz.CreateQuizSetRequest;
import com.example.demo.model.io.request.quiz.SaveQuizSetRequest;
import com.example.demo.model.io.request.quiz.UpdateQuizSetRequest;
import com.example.demo.model.io.response.object.quiz.QuizSetResponse;
import com.example.demo.model.io.response.object.quiz.SimplifiedQuizSetResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IQuizSetService {
    SimplifiedQuizSetResponse generateQuizSet(CreateQuizSetRequest request, MultipartFile file);
    QuizSetResponse saveQuizSet(SaveQuizSetRequest request);
    QuizSetResponse getQuizSetById(Long id);
    List<QuizSetResponse> getQuizSetsOfUser();
    List<QuizSetResponse> getAllQuizSets();
    QuizSetResponse deleteQuizSetById(Long id);
    QuizSetResponse updateQuizSet(Long quizSetId, UpdateQuizSetRequest request);
}
