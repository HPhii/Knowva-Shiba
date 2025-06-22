package com.example.demo.model.entity.flashcard;

import com.example.demo.model.entity.User;
import com.example.demo.model.enums.CardStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "flashcard_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    private LocalDateTime lastReviewedAt;

    private Float easeFactor;

    private Integer repetitionCount;

    private LocalDate nextDueDate;

    @Enumerated(EnumType.STRING)
    private CardStatus status;
}
