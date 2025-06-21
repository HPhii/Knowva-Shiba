package com.example.demo.model.io.response.object.flashcard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamModeFeedbackResponse {
    private Float score;
    private String whatWasCorrect;
    private String whatWasIncorrect;
    private String whatCouldHaveIncluded;
}
