package com.example.demo.service.intface;

import com.example.demo.model.immutableDTOs.Answer;
import com.example.demo.model.immutableDTOs.Question;

public interface IOpenAIService {
    Answer getResult(Question question);
}

