package com.example.demo.service.intface;

import com.example.demo.model.io.request.CreateQuizSetRequest;
import com.example.demo.model.io.request.SaveQuizSetRequest;
import com.example.demo.model.io.response.object.QuizSetResponse;
import com.example.demo.model.io.response.object.SimplifiedQuizSetResponse;
import org.springframework.web.multipart.MultipartFile;

public interface IQuizSetService {
    SimplifiedQuizSetResponse generateQuizSet(CreateQuizSetRequest request, MultipartFile file);
    QuizSetResponse saveQuizSet(SaveQuizSetRequest request);
}
