package com.example.demo.model.entity.flashcard;


import com.example.demo.model.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "flashcard_attempt")
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
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Column(name = "attempt_date", nullable = false)
    private LocalDateTime attemptDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptResult result;

    public enum AttemptResult {
        KNOW, DONT_KNOW
    }
}
