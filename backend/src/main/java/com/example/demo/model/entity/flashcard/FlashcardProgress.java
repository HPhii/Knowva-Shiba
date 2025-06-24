package com.example.demo.model.entity.flashcard;

import com.example.demo.model.entity.User;
import com.example.demo.model.enums.CardStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "`interval`")
    private Integer interval;

    private LocalDate nextDueDate;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    @OneToMany(mappedBy = "flashcardProgress", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<FlashcardAttempt> attempts = new ArrayList<>();
}
