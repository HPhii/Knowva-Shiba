package com.example.demo.model.entity.flashcard;


import com.example.demo.model.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "flashcard_set_progress_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardSetProgressSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "flashcard_set_id", nullable = false)
    private FlashcardSet flashcardSet;

    private Integer newFlashcardsPerDay;
}
