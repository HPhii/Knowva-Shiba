package com.example.demo.model.entity.quiz;

import com.example.demo.model.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

//    @Column(columnDefinition = "json")
//    private JsonNode reviewJson;
}
