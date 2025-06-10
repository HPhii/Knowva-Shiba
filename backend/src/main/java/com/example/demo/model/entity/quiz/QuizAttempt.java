package com.example.demo.model.entity.quiz;

import com.example.demo.model.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quiz_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "quiz_set_id", nullable = false)
    private QuizSet quizSet;

    private Float score;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<UserAnswer> userAnswers;

}
