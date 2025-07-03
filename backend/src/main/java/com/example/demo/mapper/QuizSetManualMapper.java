package com.example.demo.mapper;

import com.example.demo.model.entity.quiz.QuizAnswer;
import com.example.demo.model.entity.quiz.QuizQuestion;
import com.example.demo.model.entity.quiz.QuizSet;
import com.example.demo.model.io.response.object.quiz.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class QuizSetManualMapper {
    public QuizSetResponse mapToQuizSetResponse(QuizSet quizSet) {
        if (quizSet == null) return null;

        List<QuizQuestionResponse> questionResponses = quizSet.getQuestions() != null
                ? quizSet.getQuestions().stream()
                .map(this::toQuizQuestionResponse)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return new QuizSetResponse(
                quizSet.getId(),
                quizSet.getOwner().getId(),
                quizSet.getTitle(),
                quizSet.getSourceType(),
                quizSet.getLanguage(),
                quizSet.getQuestionType(),
                quizSet.getMaxQuestions(),
                quizSet.getVisibility(),
                quizSet.getCategory(),
                quizSet.getTimeLimit(),
                questionResponses
        );
    }

    public QuizQuestionResponse toQuizQuestionResponse(QuizQuestion question) {
        if (question == null) return null;

        List<QuizAnswerResponse> answerResponses = question.getAnswers() != null
                ? question.getAnswers().stream()
                .map(this::toQuizAnswerResponse)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return new QuizQuestionResponse(
                question.getId(),
                question.getQuestionText(),
                question.getQuestionHtml(),
                question.getImageUrl(),
                question.getTimeLimit(),
                question.getOrder(),
                answerResponses
        );
    }

    public QuizAnswerResponse toQuizAnswerResponse(QuizAnswer answer) {
        if (answer == null) return null;

        return new QuizAnswerResponse(
                answer.getId(),
                answer.getAnswerText(),
                answer.getIsCorrect()
        );
    }

    private List<QuizQuestionResponse> toQuizQuestionResponseList(List<QuizQuestion> questions) {
        if (questions == null) return new ArrayList<>();
        return questions.stream()
                .map(this::toQuizQuestionResponse)
                .collect(Collectors.toList());
    }

    private List<QuizAnswerResponse> toQuizAnswerResponseList(List<QuizAnswer> answers) {
        if (answers == null) return new ArrayList<>();
        return answers.stream()
                .map(this::toQuizAnswerResponse)
                .collect(Collectors.toList());
    }

    public SimplifiedQuizSetResponse mapToSimplifiedQuizSetResponse(QuizSet quizSet) {
        if (quizSet == null) return null;

        List<SimplifiedQuizQuestionResponse> questions = quizSet.getQuestions() != null
                ? quizSet.getQuestions().stream()
                .map(this::toSimplifiedQuizQuestionResponse)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return new SimplifiedQuizSetResponse(
                quizSet.getTitle(),
                quizSet.getSourceType().name(),
                quizSet.getLanguage(),
                quizSet.getQuestionType().name(),
                quizSet.getMaxQuestions(),
                quizSet.getVisibility().name(),
                quizSet.getCategory() != null ? quizSet.getCategory().name() : null,
                quizSet.getTimeLimit(),
                questions
        );
    }

    private SimplifiedQuizQuestionResponse toSimplifiedQuizQuestionResponse(QuizQuestion question) {
        List<SimplifiedQuizAnswerResponse> answers = question.getAnswers() != null
                ? question.getAnswers().stream()
                .map(ans -> new SimplifiedQuizAnswerResponse(ans.getAnswerText(), ans.getIsCorrect()))
                .collect(Collectors.toList())
                : new ArrayList<>();

        return new SimplifiedQuizQuestionResponse(
                question.getQuestionText(),
                question.getQuestionHtml(),
                question.getImageUrl(),
                question.getTimeLimit(),
                question.getOrder(),
                answers
        );
    }

    //map To QuizSetResponse List from List<QuizSet>
    public List<QuizSetResponse> mapToQuizSetResponseList(List<QuizSet> quizSets) {
        if (quizSets == null) return new ArrayList<>();
        return quizSets.stream()
                .map(this::mapToQuizSetResponse)
                .collect(Collectors.toList());
    }

}