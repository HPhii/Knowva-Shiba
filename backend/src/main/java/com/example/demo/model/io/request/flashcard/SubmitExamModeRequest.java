package com.example.demo.model.io.request.flashcard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitExamModeRequest {
    private Long flashcardId;
    private String userAnswer;
}

