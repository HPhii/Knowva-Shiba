package com.example.demo.model.io.response.paged;

import com.example.demo.model.io.response.object.quiz.QuizSetResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagedQuizSetResponse {
    private List<QuizSetResponse> quizSets;
    private long totalElements;
    private int totalPages;
    private int currentPage;
}
