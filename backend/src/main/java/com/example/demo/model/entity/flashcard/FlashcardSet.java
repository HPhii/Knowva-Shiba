package com.example.demo.model.entity.flashcard;

import com.example.demo.model.entity.User;
import com.example.demo.model.enums.SourceType;
import com.example.demo.model.enums.Visibility;
import com.example.demo.model.enums.CardType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "flashcard_sets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User owner;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    private SourceType sourceType;

    @Column
    private String language;

    @Enumerated(EnumType.STRING)
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "flashcardSet", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Flashcard> flashcards;

    @OneToMany(mappedBy = "flashcardSet", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<FlashcardAttempt> attempts;

    @OneToMany(mappedBy = "flashcardSet", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<FlashcardAccessControl> accessControls;
}
