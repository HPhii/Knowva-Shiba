package com.example.demo.model.io.request.quiz;

import com.example.demo.model.enums.Category;
import com.example.demo.model.enums.QuestionType;
import com.example.demo.model.enums.SourceType;
import com.example.demo.model.enums.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuizSetRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotNull(message = "Source type is required")
    private SourceType sourceType;

    @NotBlank(message = "Language is required")
    private String language;

    @NotNull(message = "Question type is required")
    private QuestionType questionType;

    private Integer maxQuestions;

    @NotNull(message = "Visibility is required")
    private Visibility visibility;
    
    private Category category;

    private Integer timeLimit;
}
