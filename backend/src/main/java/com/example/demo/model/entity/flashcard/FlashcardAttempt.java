package com.example.demo.model.entity.flashcard;

import com.example.demo.model.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "flashcard_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "flashcard_set_id", nullable = false)
    private FlashcardSet flashcardSet;

    @ManyToOne
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Column(columnDefinition = "TEXT")
    private String userAnswer;

    private Float score;

//    @Column(columnDefinition = "json")
//    private JsonNode aiFeedback;

    private LocalDateTime attemptedAt;
}
