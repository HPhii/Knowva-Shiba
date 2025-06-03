package com.example.demo.model.entity.quiz;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;

    @Column(columnDefinition = "TEXT")
    private String answerText;

    private Boolean isCorrect;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
