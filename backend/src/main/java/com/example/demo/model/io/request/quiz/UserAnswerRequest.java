package com.example.demo.model.io.request.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswerRequest {
    private Long questionId;
    private Long selectedAnswerId;
}